package com.haanibiriyani.hattrick.network;

import com.haanibiriyani.hattrick.block.entity.EnforcementBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateEnforcementBlockPacket {
    private final BlockPos pos;
    private final int radius;
    private final String command;

    public UpdateEnforcementBlockPacket(BlockPos pos, int radius, String command) {
        this.pos = pos;
        this.radius = radius;
        this.command = command;
    }

    public static void encode(UpdateEnforcementBlockPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.radius);
        buffer.writeUtf(packet.command);
    }

    public static UpdateEnforcementBlockPacket decode(FriendlyByteBuf buffer) {
        return new UpdateEnforcementBlockPacket(
                buffer.readBlockPos(),
                buffer.readInt(),
                buffer.readUtf()
        );
    }

    public static void handle(UpdateEnforcementBlockPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.isCreative()) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                if (blockEntity instanceof EnforcementBlockEntity enforcementBlock) {
                    enforcementBlock.setRadius(packet.radius);
                    enforcementBlock.setCommand(packet.command);
                }
            }
        });
        context.setPacketHandled(true);
    }
}