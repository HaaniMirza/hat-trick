package com.haanibiriyani.hattrick;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID)
public class HatManTransformationHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        boolean isTransformed = serverPlayer.getPersistentData().getBoolean("HatManTransformed");
        boolean wasHidden = serverPlayer.getPersistentData().getBoolean("TabListHidden");

        // Check every 20 ticks (once per second) to update tab list visibility
        if (serverPlayer.tickCount % 20 == 0) {
            if (isTransformed && !wasHidden) {
                // Remove from tab list
                hidePlayerFromTabList(serverPlayer);
                serverPlayer.getPersistentData().putBoolean("TabListHidden", true);
            } else if (!isTransformed && wasHidden) {
                // Add back to tab list
                showPlayerInTabList(serverPlayer);
                serverPlayer.getPersistentData().putBoolean("TabListHidden", false);
            }
        }
    }

    private static void hidePlayerFromTabList(ServerPlayer player) {
        // Send packet to all players to remove this player from their tab list
        ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));

        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != player) {
                otherPlayer.connection.send(removePacket);
            }
        }
    }

    private static void showPlayerInTabList(ServerPlayer player) {
        // Send packet to all players to add this player back to their tab list
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
            // Cancel the normal chat message
            event.setCanceled(true);

            // Create custom formatted message: purple, bold, italic, no name
            String message = event.getMessage().getString();

            MutableComponent formattedMessage = Component.literal(message)
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.DARK_PURPLE)
                            .withBold(true)
                            .withItalic(true)
                    );

            // Send to all players
            player.getServer().getPlayerList().broadcastSystemMessage(formattedMessage, false);
        }
    }
}