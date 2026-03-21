package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.network.HatManSyncPacket;
import com.haanibiriyani.hattrick.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID)
public class HatManTransformationHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        boolean isTransformed = serverPlayer.getPersistentData().getBoolean("HatManTransformed");
        boolean wasHidden = serverPlayer.getPersistentData().getBoolean("TabListHidden");

        if (serverPlayer.tickCount % 20 == 0) {
            if (isTransformed && !wasHidden) {
                hidePlayerFromTabList(serverPlayer);
                serverPlayer.getPersistentData().putBoolean("TabListHidden", true);
            } else if (!isTransformed && wasHidden) {
                showPlayerInTabList(serverPlayer);
                serverPlayer.getPersistentData().putBoolean("TabListHidden", false);
            }
        }
    }

    /**
     * When a player joins, send them sync packets for every currently transformed
     * player so their client immediately shows the correct Hat Man skin on each one.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer joiningPlayer)) return;

        List<ServerPlayer> allPlayers = joiningPlayer.getServer().getPlayerList().getPlayers();

        for (ServerPlayer onlinePlayer : allPlayers) {
            // Skip the joining player themselves — TransformPacket already
            // broadcasts to ALL when they toggle, including themselves
            if (onlinePlayer == joiningPlayer) continue;

            if (onlinePlayer.getPersistentData().getBoolean("HatManTransformed")) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> joiningPlayer),
                        new HatManSyncPacket(onlinePlayer.getUUID(), true)
                );
            }
        }
    }

    private static void hidePlayerFromTabList(ServerPlayer player) {
        ClientboundPlayerInfoRemovePacket removePacket =
                new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));

        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != player) {
                otherPlayer.connection.send(removePacket);
            }
        }
    }

    private static void showPlayerInTabList(ServerPlayer player) {
        ClientboundPlayerInfoUpdatePacket addPacket = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                player
        );

        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != player) {
                otherPlayer.connection.send(addPacket);
            }
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        boolean isTransformed = player.getPersistentData().getBoolean("HatManTransformed");

        if (isTransformed) {
            event.setCanceled(true);

            String message = event.getMessage().getString();
            MutableComponent formattedMessage = Component.literal(message)
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.DARK_PURPLE)
                            .withBold(true)
                            .withItalic(true)
                    );

            player.getServer().getPlayerList().broadcastSystemMessage(formattedMessage, false);
        }
    }
}