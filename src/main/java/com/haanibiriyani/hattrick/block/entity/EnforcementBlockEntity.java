package com.haanibiriyani.hattrick.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnforcementBlockEntity extends BlockEntity implements MenuProvider {
    private int radius = 10;
    private String command = "";
    private final Set<UUID> playersInRange = new HashSet<>();
    private final Map<UUID, CompoundTag> playerStates = new HashMap<>();

    public EnforcementBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENFORCEMENT_BLOCK.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Enforcement Block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.haanibiriyani.hattrick.block.menu.EnforcementBlockMenu(containerId, playerInventory, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnforcementBlockEntity blockEntity) {
        if (level.isClientSide || level.getGameTime() % 20 != 0) return; // Check every second

        blockEntity.checkPlayersInRange((ServerLevel) level);
    }

    private void checkPlayersInRange(ServerLevel level) {
        if (command.isEmpty()) return;

        AABB range = new AABB(worldPosition).inflate(radius);
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(
                ServerPlayer.class,
                range,
                p -> !p.isSpectator()
        );

        Set<UUID> currentPlayers = new HashSet<>();

        // Check players entering or staying in range
        for (ServerPlayer player : nearbyPlayers) {
            UUID playerId = player.getUUID();
            currentPlayers.add(playerId);

            if (!playersInRange.contains(playerId)) {
                // Player entered range
                onPlayerEnter(player);
            }
        }

        // Check players who left range
        Iterator<UUID> iterator = playersInRange.iterator();
        while (iterator.hasNext()) {
            UUID playerId = iterator.next();
            if (!currentPlayers.contains(playerId)) {
                // Player left range
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
                if (player != null) {
                    onPlayerExit(player);
                }
                iterator.remove();
            }
        }

        playersInRange.clear();
        playersInRange.addAll(currentPlayers);
    }

    private void onPlayerEnter(ServerPlayer player) {
        // Save player state before applying effect
        savePlayerState(player);

        // Apply the command effect
        applyCommand(player);
    }

    private void onPlayerExit(ServerPlayer player) {
        // Restore player state
        restorePlayerState(player);
    }

    private void savePlayerState(ServerPlayer player) {
        CompoundTag state = new CompoundTag();
        UUID playerId = player.getUUID();

        if (command.toLowerCase().startsWith("/effect")) {
            // Save current effects (we'll just clear them for now)
            state.putString("type", "effect");
        } else if (command.toLowerCase().startsWith("/gamemode")) {
            // Save current gamemode
            state.putString("type", "gamemode");
            state.putString("gamemode", player.gameMode.getGameModeForPlayer().getName());
        }

        playerStates.put(playerId, state);
    }

    private void restorePlayerState(ServerPlayer player) {
        UUID playerId = player.getUUID();
        CompoundTag state = playerStates.get(playerId);

        if (state == null) return;

        String type = state.getString("type");

        if ("effect".equals(type)) {
            // Parse and remove the effect that was applied
            String effectName = parseEffectFromCommand(command);
            if (effectName != null) {
                MobEffect effect = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(
                        new net.minecraft.resources.ResourceLocation(effectName)
                );
                if (effect != null) {
                    player.removeEffect(effect);
                }
            }
        } else if ("gamemode".equals(type)) {
            // Restore previous gamemode
            String gamemodeName = state.getString("gamemode");
            GameType gameType = GameType.byName(gamemodeName, GameType.SURVIVAL);
            player.setGameMode(gameType);
        }

        playerStates.remove(playerId);
    }

    private void applyCommand(ServerPlayer player) {
        if (command.isEmpty()) return;

        // Execute command on the player
        String processedCommand = command.replace("@p", player.getName().getString());

        player.getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                processedCommand.startsWith("/") ? processedCommand.substring(1) : processedCommand
        );
    }

    private String parseEffectFromCommand(String cmd) {
        // Parse effect name from command like "/effect give @p minecraft:speed"
        String[] parts = cmd.toLowerCase().split(" ");
        if (parts.length >= 4 && parts[0].equals("/effect") && parts[1].equals("give")) {
            String effect = parts[3];
            // Remove namespace if present
            if (effect.contains(":")) {
                return effect;
            } else {
                return "minecraft:" + effect;
            }
        }
        return null;
    }

    public void clearAllEffects() {
        if (level instanceof ServerLevel serverLevel) {
            for (UUID playerId : playersInRange) {
                ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(playerId);
                if (player != null) {
                    restorePlayerState(player);
                }
            }
        }
        playersInRange.clear();
        playerStates.clear();
    }

    // Getters and setters
    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(1, Math.min(radius, 100)); // Clamp between 1 and 100
        setChanged();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Radius", radius);
        tag.putString("Command", command);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        radius = tag.getInt("Radius");
        command = tag.getString("Command");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
}