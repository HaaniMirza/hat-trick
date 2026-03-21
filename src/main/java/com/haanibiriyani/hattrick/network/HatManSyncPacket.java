package com.haanibiriyani.hattrick.network;

import com.haanibiriyani.hattrick.client.HatManClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class HatManSyncPacket {

    private final UUID playerUUID;
    private final boolean transformed;

    public HatManSyncPacket(UUID playerUUID, boolean transformed) {
        this.playerUUID  = playerUUID;
        this.transformed = transformed;
    }

    public static void encode(HatManSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerUUID);
        buffer.writeBoolean(packet.transformed);
    }

    public static HatManSyncPacket decode(FriendlyByteBuf buffer) {
        return new HatManSyncPacket(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(HatManSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                // DistExecutor ensures this only runs on the client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        HatManClientHandler.setTransformed(packet.playerUUID, packet.transformed)
                )
        );
        context.setPacketHandled(true);
    }
}