package com.haanibiriyani.hattrick.command;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import com.haanibiriyani.hattrick.entity.ModEntities;
import com.haanibiriyani.hattrick.entity.ai.EnforcerAggroManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class HatTrickCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hattrick")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("target")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> setTarget(context,
                                        EntityArgument.getPlayer(context, "player"))))
                        .then(Commands.literal("clear")
                                .executes(HatTrickCommand::clearTarget)))
                .then(Commands.literal("aggro")
                        .then(Commands.argument("aggressive", BoolArgumentType.bool())
                                .executes(context -> setAggro(context,
                                        BoolArgumentType.getBool(context, "aggressive")))))
                .then(Commands.literal("config")
                        .then(Commands.literal("groupsize")
                                .then(Commands.argument("size", IntegerArgumentType.integer(1))
                                        .executes(context -> setGroupSize(context,
                                                IntegerArgumentType.getInteger(context, "size"))))
                                // Also allow querying the current value with no argument
                                .executes(HatTrickCommand::getGroupSize)))
        );
    }

    private static int setGroupSize(CommandContext<CommandSourceStack> context, int size) {
        EnforcerAggroManager.setMinGroupSize(size);
        context.getSource().sendSuccess(() -> Component.literal(
                "Enforcer group aggro threshold set to " + size + " player(s). " +
                        "All active warnings have been cleared."
        ), true);
        return size;
    }

    private static int getGroupSize(CommandContext<CommandSourceStack> context) {
        int current = EnforcerAggroManager.getMinGroupSize();
        context.getSource().sendSuccess(() -> Component.literal(
                "Current Enforcer group aggro threshold: " + current + " player(s)."
        ), false);
        return current;
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
            enforcer.addAggroTarget(targetPlayer.getUUID());
            count++;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.literal(
                "Set " + finalCount + " Enforcer(s) to target " +
                        targetPlayer.getName().getString()
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
            enforcer.addAggroTarget(null);
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
                "Set " + finalCount + " Enforcer(s) to " +
                        (aggressive ? "aggressive" : "passive")
        ), true);
        return count;
    }
}