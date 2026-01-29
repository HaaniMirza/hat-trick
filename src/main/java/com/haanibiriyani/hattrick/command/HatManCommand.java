package com.haanibiriyani.hattrick.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class HatManCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hatman")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> teleportHatMan(context, EntityArgument.getPlayer(context, "target"))))
        );
    }

    private static int teleportHatMan(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        // Find player wearing Hat Man's Hat
        ServerPlayer hatManPlayer = findHatManPlayer(level);

        if (hatManPlayer == null) {
            source.sendFailure(Component.literal("No player is wearing The Hat Man's Hat"));
            return 0;
        }

        // Transform the Hat Man if not already transformed
        if (!hatManPlayer.getPersistentData().getBoolean("HatManTransformed")) {
            hatManPlayer.getPersistentData().putBoolean("HatManTransformed", true);
            hatManPlayer.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    true
            ));

            // Spawn transformation particles at Hat Man's current location
            spawnTransformationParticles(level, hatManPlayer.position());
        }

        // Calculate position in front of target player
        Vec3 targetPos = targetPlayer.position();
        Vec3 targetLook = targetPlayer.getLookAngle();

        // Place Hat Man 3 blocks in front of the target, facing them
        Vec3 teleportPos = targetPos.add(targetLook.scale(3.0));

        // Spawn particles at destination before teleport
        spawnTeleportParticles(level, teleportPos);

        // Teleport Hat Man
        hatManPlayer.teleportTo(level, teleportPos.x, teleportPos.y, teleportPos.z,
                java.util.EnumSet.noneOf(net.minecraft.world.entity.RelativeMovement.class),
                0, 0);

        // Make Hat Man face the target player
        Vec3 directionToTarget = targetPos.subtract(teleportPos).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-directionToTarget.x, directionToTarget.z));
        float pitch = (float) Math.toDegrees(Math.asin(-directionToTarget.y));

        hatManPlayer.setYRot(yaw);
        hatManPlayer.setXRot(pitch);
        hatManPlayer.setYHeadRot(yaw);

        // Spawn arrival particles
        spawnArrivalParticles(level, teleportPos);

        // Send success message
        source.sendSuccess(() -> Component.literal(
                "The Hat Man has appeared before " + targetPlayer.getName().getString()
        ), true);

        // Optional: Send a message to the target player
        targetPlayer.sendSystemMessage(Component.literal("§5§l§oThe Hat Man appears before you..."));

        return 1;
    }

    private static ServerPlayer findHatManPlayer(ServerLevel level) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            ItemStack helmet = player.getInventory().getArmor(3);
            if (helmet.hasTag() && helmet.getTag().getBoolean("HatMansHat")) {
                return player;
            }
        }
        return null;
    }

    private static void spawnTransformationParticles(ServerLevel level, Vec3 pos) {
        // Dark smoke spiral at current location
        for (int i = 0; i < 30; i++) {
            double angle = (Math.PI * 2 * i) / 30;
            double radius = 1.0;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    pos.x + offsetX,
                    pos.y + 1.0,
                    pos.z + offsetZ,
                    1,
                    0.0, 0.1, 0.0, 0.02
            );
        }

        // Soul particles
        level.sendParticles(
                ParticleTypes.SOUL,
                pos.x, pos.y + 1.0, pos.z,
                20,
                0.5, 0.5, 0.5, 0.1
        );
    }

    private static void spawnTeleportParticles(ServerLevel level, Vec3 pos) {
        // Portal particles at destination (preview of arrival)
        level.sendParticles(
                ParticleTypes.PORTAL,
                pos.x, pos.y + 1.0, pos.z,
                50,
                0.5, 1.0, 0.5, 0.5
        );

        // Reverse soul particles
        level.sendParticles(
                ParticleTypes.SOUL,
                pos.x, pos.y + 1.0, pos.z,
                15,
                0.3, 0.5, 0.3, 0.05
        );
    }

    private static void spawnArrivalParticles(ServerLevel level, Vec3 pos) {
        // Dramatic arrival effect

        // Explosion of dark smoke
        for (int i = 0; i < 50; i++) {
            double angle = (Math.PI * 2 * i) / 50;
            double radius = 1.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x + offsetX,
                    pos.y + 1.0,
                    pos.z + offsetZ,
                    1,
                    0.0, 0.2, 0.0, 0.03
            );
        }

        // Large smoke burst
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                pos.x, pos.y + 1.0, pos.z,
                30,
                0.5, 0.5, 0.5, 0.1
        );

        // Ominous purple/soul particles
        level.sendParticles(
                ParticleTypes.SOUL,
                pos.x, pos.y + 1.0, pos.z,
                40,
                0.5, 1.0, 0.5, 0.15
        );

        // Flash of darkness particles
        level.sendParticles(
                ParticleTypes.SQUID_INK,
                pos.x, pos.y + 1.0, pos.z,
                20,
                0.3, 0.5, 0.3, 0.1
        );
    }
}