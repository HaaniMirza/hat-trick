package com.haanibiriyani.hattrick.network;

import com.haanibiriyani.hattrick.HatTrickMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HatTrickMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        CHANNEL.registerMessage(
                packetId++,
                TransformPacket.class,
                TransformPacket::encode,
                TransformPacket::decode,
                TransformPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                UpdateEnforcementBlockPacket.class,
                UpdateEnforcementBlockPacket::encode,
                UpdateEnforcementBlockPacket::decode,
                UpdateEnforcementBlockPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                SyncEnforcementBlockPacket.class,
                SyncEnforcementBlockPacket::encode,
                SyncEnforcementBlockPacket::decode,
                SyncEnforcementBlockPacket::handle
        );
    }
}