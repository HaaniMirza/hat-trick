package com.haanibiriyani.hattrick.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class TransformPacket {

    public TransformPacket() {}

    public static void encode(TransformPacket packet, FriendlyByteBuf buffer) {}

    public static TransformPacket decode(FriendlyByteBuf buffer) {
        return new TransformPacket();
    }

    public static void handle(TransformPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack helmet = player.getInventory().getArmor(3);
            if (!helmet.hasTag() || !helmet.getTag().getBoolean("HatMansHat")) return;

            boolean isTransformed = player.getPersistentData().getBoolean("HatManTransformed");

            if (isTransformed) {
                // Untransform
                player.getPersistentData().putBoolean("HatManTransformed", false);
                spawnTransformationParticles(player, false);
            } else {
                // Transform
                player.getPersistentData().putBoolean("HatManTransformed", true);
                spawnTransformationParticles(player, true);
            }

            // Broadcast new state to all clients so skins update everywhere
            boolean newState = !isTransformed;
            ModNetwork.CHANNEL.send(
                    PacketDistributor.ALL.noArg(),
                    new HatManSyncPacket(player.getUUID(), newState)
            );
        });
        context.setPacketHandled(true);
    }

    private static void spawnTransformationParticles(ServerPlayer player, boolean transforming) {
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        if (transforming) {
            for (int i = 0; i < 50; i++) {
                double angle   = (Math.PI * 2 * i) / 50;
                double radius  = 1.0;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (i / 50.0) * 2.0;

                level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                        x + offsetX, y + offsetY, z + offsetZ, 1, 0.0, 0.1, 0.0, 0.01);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x + offsetX * 0.5, y + offsetY, z + offsetZ * 0.5, 1, 0.0, 0.05, 0.0, 0.02);
            }

            for (int i = 0; i < 20; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 2.0;
                double offsetY = player.getRandom().nextDouble() * 2.0;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2.0;
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                        x + offsetX, y + offsetY, z + offsetZ, 1, 0.0, 0.1, 0.0, 0.05);
            }
        } else {
            for (int i = 0; i < 30; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 1.5;
                double offsetY = player.getRandom().nextDouble() * 2.0;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 1.5;
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        x + offsetX, y + offsetY, z + offsetZ, 1, 0.0, 0.1, 0.0, 0.02);
            }
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                    x, y + 1, z, 15, 0.5, 0.5, 0.5, 0.1);
        }
    }
}