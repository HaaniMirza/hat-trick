package com.haanibiriyani.hattrick.network;

import com.haanibiriyani.hattrick.block.entity.EnforcementBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEnforcementBlockPacket {
    private final BlockPos pos;
    private final int radius;
    private final String command;

    public SyncEnforcementBlockPacket(BlockPos pos, int radius, String command) {
        this.pos = pos;
        this.radius = radius;
        this.command = command;
    }

    public static void encode(SyncEnforcementBlockPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.radius);
        buffer.writeUtf(packet.command);
    }

    public static SyncEnforcementBlockPacket decode(FriendlyByteBuf buffer) {
        return new SyncEnforcementBlockPacket(
                buffer.readBlockPos(),
                buffer.readInt(),
                buffer.readUtf()
        );
    }

    public static void handle(SyncEnforcementBlockPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                BlockEntity blockEntity = mc.level.getBlockEntity(packet.pos);
                if (blockEntity instanceof EnforcementBlockEntity enforcementBlock) {
                    enforcementBlock.setRadius(packet.radius);
                    enforcementBlock.setCommand(packet.command);
                }
            }
        });
        context.setPacketHandled(true);
    }
}