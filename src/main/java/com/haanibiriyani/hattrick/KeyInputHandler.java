package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.network.ModNetwork;
import com.haanibiriyani.hattrick.network.TransformPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null && ModKeyBindings.TRANSFORM_KEY.consumeClick()) {
                // Check if player is wearing Hat Man's Hat
                ItemStack helmet = mc.player.getInventory().getArmor(3);
                if (helmet.hasTag() && helmet.getTag().getBoolean("HatMansHat")) {
                    // Send packet to server to toggle transformation
                    ModNetwork.CHANNEL.sendToServer(new TransformPacket());
                }
            }
        }
    }
}