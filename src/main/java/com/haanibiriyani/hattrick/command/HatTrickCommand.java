package com.haanibiriyani.hattrick.command;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import com.haanibiriyani.hattrick.entity.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class HatTrickCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hattrick")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("target")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> setTarget(context, EntityArgument.getPlayer(context, "player"))))
                        .then(Commands.literal("clear")
                                .executes(HatTrickCommand::clearTarget)))
                .then(Commands.literal("aggro")
                        .then(Commands.argument("aggressive", BoolArgumentType.bool())
                                .executes(context -> setAggro(context, BoolArgumentType.getBool(context, "aggressive")))))
        );
    }

    private static int setTarget(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        List<EnforcerEntity> enforcers = level.getEntitiesOfClass(
                EnforcerEntity.class,
                new AABB(
                        source.getPosition().add(-100, -100, -100),
                        source.getPosition().add(100, 100, 100)
                )
        );

        if (enforcers.isEmpty()) {
            source.sendFailure(Component.literal("No Enforcers found nearby"));
            return 0;
        }

        int count = 0;
        for (EnforcerEntity enforcer : enforcers) {
            enforcer.setTargetPlayer(targetPlayer.getUUID());
            count++;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.literal(
                "Set " + finalCount + " Enforcer(s) to target " + targetPlayer.getName().getString()
        ), true);

        return count;
    }

    private static int clearTarget(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        List<EnforcerEntity> enforcers = level.getEntitiesOfClass(
                EnforcerEntity.class,
                new AABB(
                        source.getPosition().add(-100, -100, -100),
                        source.getPosition().add(100, 100, 100)
                )
        );

        if (enforcers.isEmpty()) {
            source.sendFailure(Component.literal("No Enforcers found nearby"));
            return 0;
        }

        int count = 0;
        for (EnforcerEntity enforcer : enforcers) {
            enforcer.setTargetPlayer(null);
            count++;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.literal(
                "Cleared target for " + finalCount + " Enforcer(s)"
        ), true);

        return count;
    }

    private static int setAggro(CommandContext<CommandSourceStack> context, boolean aggressive) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        List<EnforcerEntity> enforcers = level.getEntitiesOfClass(
                EnforcerEntity.class,
                new AABB(
                        source.getPosition().add(-100, -100, -100),
                        source.getPosition().add(100, 100, 100)
                )
        );

        if (enforcers.isEmpty()) {
            source.sendFailure(Component.literal("No Enforcers found nearby"));
            return 0;
        }

        int count = 0;
        for (EnforcerEntity enforcer : enforcers) {
            enforcer.setAggressive(aggressive);
            count++;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.literal(
                "Set " + finalCount + " Enforcer(s) to " + (aggressive ? "aggressive" : "passive")
        ), true);

        return count;
    }
}