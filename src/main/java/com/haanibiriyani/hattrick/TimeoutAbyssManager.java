package com.haanibiriyani.hattrick;

import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class TimeoutAbyssManager {

    public static final long TIMEOUT_DURATION = 6000L;

    // Fixed spawn point inside the Timeout Abyss (public so the event handler can reference it)
    public static final double ABYSS_SPAWN_X = 0.5D;
    public static final double ABYSS_SPAWN_Y = 1.0D;
    public static final double ABYSS_SPAWN_Z = 0.5D;

    // -------------------------------------------------------------------------
    // TimeoutData — snapshot of a player's state at the moment of timeout
    // -------------------------------------------------------------------------
    public static class TimeoutData {
        public ResourceKey<Level> originalDimension;
        public double originalX, originalY, originalZ;
        public float originalYaw, originalPitch;
        public ListTag inventoryNbt;  // full inventory serialised to NBT
        public int xpLevel;
        public float xpProgress;
        public long ticksRemaining;

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString("dimension", originalDimension.location().toString());
            tag.putDouble("x", originalX);
            tag.putDouble("y", originalY);
            tag.putDouble("z", originalZ);
            tag.putFloat("yaw", originalYaw);
            tag.putFloat("pitch", originalPitch);
            tag.put("inventory", inventoryNbt);
            tag.putInt("xp_level", xpLevel);
            tag.putFloat("xp_progress", xpProgress);
            tag.putLong("ticks_remaining", ticksRemaining);
            return tag;
        }

        public static TimeoutData fromNbt(CompoundTag tag) {
            TimeoutData td = new TimeoutData();
            td.originalDimension = ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(tag.getString("dimension")));
            td.originalX     = tag.getDouble("x");
            td.originalY     = tag.getDouble("y");
            td.originalZ     = tag.getDouble("z");
            td.originalYaw   = tag.getFloat("yaw");
            td.originalPitch = tag.getFloat("pitch");
            td.inventoryNbt  = tag.getList("inventory", Tag.TAG_COMPOUND);
            td.xpLevel       = tag.getInt("xp_level");
            td.xpProgress    = tag.getFloat("xp_progress");
            td.ticksRemaining = tag.getLong("ticks_remaining");
            return td;
        }
    }

    // -------------------------------------------------------------------------
    // TimeoutSavedData — persists timeout state across server restarts
    // -------------------------------------------------------------------------
    public static class TimeoutSavedData extends SavedData {

        private final Map<UUID, TimeoutData> dataMap = new HashMap<>();

        public static TimeoutSavedData load(CompoundTag nbt) {
            TimeoutSavedData instance = new TimeoutSavedData();
            ListTag list = nbt.getList("timeouts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                UUID uuid = entry.getUUID("player_uuid");
                instance.dataMap.put(uuid, TimeoutData.fromNbt(entry));
            }
            return instance;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag list = new ListTag();
            for (Map.Entry<UUID, TimeoutData> entry : dataMap.entrySet()) {
                CompoundTag entryTag = entry.getValue().toNbt();
                entryTag.putUUID("player_uuid", entry.getKey());
                list.add(entryTag);
            }
            tag.put("timeouts", list);
            return tag;
        }

        public boolean isInTimeout(UUID uuid)              { return dataMap.containsKey(uuid); }
        public TimeoutData getPlayerData(UUID uuid)        { return dataMap.get(uuid); }
        public Set<Map.Entry<UUID, TimeoutData>> getEntries() { return dataMap.entrySet(); }

        public void addPlayer(UUID uuid, TimeoutData td) { dataMap.put(uuid, td); setDirty(); }
        public void removePlayer(UUID uuid)              { dataMap.remove(uuid);   setDirty(); }
    }

    // -------------------------------------------------------------------------
    // Core logic
    // -------------------------------------------------------------------------

    private static TimeoutSavedData getSavedData(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                TimeoutSavedData::load,
                TimeoutSavedData::new,
                "timeout_void_data"
        );
    }

    /** Strip the player's items/XP, store them, and teleport to the Timeout Abyss. */
    public static void sendToTimeout(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        TimeoutSavedData data = getSavedData(server);
        if (data.isInTimeout(player.getUUID())) return; // Guard against double-timeout

        TimeoutData td        = new TimeoutData();
        td.originalDimension  = player.level().dimension();
        td.originalX          = player.getX();
        td.originalY          = player.getY();
        td.originalZ          = player.getZ();
        td.originalYaw        = player.getYRot();
        td.originalPitch      = player.getXRot();
        td.xpLevel            = player.experienceLevel;
        td.xpProgress         = player.experienceProgress;
        td.ticksRemaining     = TIMEOUT_DURATION;

        // Serialise full inventory (main 0-35, armor 36-39, offhand 40) to NBT
        td.inventoryNbt = new ListTag();
        player.getInventory().save(td.inventoryNbt);

        // Strip everything — no items or XP enter the void
        player.getInventory().clearContent();
        player.setExperienceLevels(0);
        player.experienceProgress = 0.0f;
        player.totalExperience    = 0;

        // Restore health so the cancelled death doesn't leave the player at 0 HP
        player.setHealth(player.getMaxHealth());

        data.addPlayer(player.getUUID(), td);

        ServerLevel abyssLevel = server.getLevel(ModDimensions.TIMEOUT_ABYSS_LEVEL_KEY);
        if (abyssLevel != null) {
            player.teleportTo(abyssLevel, ABYSS_SPAWN_X, ABYSS_SPAWN_Y, ABYSS_SPAWN_Z,
                    player.getYRot(), 0.0F);
        }

        server.getPlayerList().broadcastSystemMessage(
                Component.literal(player.getName().getString() + " was sent to Timeout"),
                false
        );
    }

    /** Teleport the player back and restore their stored items (minus Curse of Vanishing). */
    public static void returnFromTimeout(ServerPlayer player, MinecraftServer server) {
        TimeoutSavedData data = getSavedData(server);
        TimeoutData td        = data.getPlayerData(player.getUUID());
        if (td == null) return;

        data.removePlayer(player.getUUID());

        ServerLevel originalLevel = server.getLevel(td.originalDimension);
        if (originalLevel == null) originalLevel = server.overworld(); // Fallback

        player.teleportTo(originalLevel, td.originalX, td.originalY, td.originalZ,
                td.originalYaw, td.originalPitch);

        // Load saved items into a temporary inventory so we can filter slot-by-slot
        player.getInventory().clearContent();
        Inventory tempInv = new Inventory(player);
        tempInv.load(td.inventoryNbt);

        for (int i = 0; i < tempInv.getContainerSize(); i++) {
            ItemStack stack = tempInv.getItem(i);
            if (!stack.isEmpty() &&
                    EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.VANISHING_CURSE, stack) == 0) {
                player.getInventory().setItem(i, stack);
            }
            // Items with Curse of Vanishing are simply discarded — they don't drop
        }

        player.setExperienceLevels(td.xpLevel);
        player.experienceProgress = td.xpProgress;
    }

    /**
     * Called every server tick. Decrements timers and returns players when time expires.
     * Dirty is written every second (20 ticks) so crash recovery loses at most ~1 second.
     */
    public static void tick(MinecraftServer server) {
        TimeoutSavedData data = getSavedData(server);
        if (data.getEntries().isEmpty()) return;

        List<UUID> toReturn = new ArrayList<>();

        for (Map.Entry<UUID, TimeoutData> entry : data.getEntries()) {
            if (entry.getValue().ticksRemaining > 0) {
                entry.getValue().ticksRemaining--;
                if (entry.getValue().ticksRemaining <= 0) {
                    toReturn.add(entry.getKey());
                }
            }
        }

        if (server.getTickCount() % 20 == 0) {
            data.setDirty();
        }

        for (UUID uuid : toReturn) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                returnFromTimeout(player, server);
            }
            // If the player is offline, their entry remains with ticksRemaining = 0
            // and onPlayerLogin() will process them the moment they reconnect
        }
    }

    /**
     * Call this on player login. Handles two cases:
     * 1. Timer already expired while they were offline → return them immediately.
     * 2. Timer still running → ensure they're in the void (guards against edge-case respawns).
     */
    public static void onPlayerLogin(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        TimeoutSavedData data = getSavedData(server);
        if (!data.isInTimeout(player.getUUID())) return;

        TimeoutData td = data.getPlayerData(player.getUUID());
        if (td == null) return;

        if (td.ticksRemaining <= 0) {
            returnFromTimeout(player, server);
        } else {
            // Timer still running — make sure they're in the void
            ServerLevel voidLevel = server.getLevel(ModDimensions.TIMEOUT_ABYSS_LEVEL_KEY);
            if (voidLevel != null) {
                player.teleportTo(voidLevel, ABYSS_SPAWN_X, ABYSS_SPAWN_Y, ABYSS_SPAWN_Z,
                        player.getYRot(), 0.0F);
            }
        }
    }
}