package com.haanibiriyani.hattrick;


import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TeleportParticleHandler {

    @SubscribeEvent
    public static void onCommandExecute(CommandEvent event) {
        ParseResults<CommandSourceStack> parseResults = event.getParseResults();
        CommandContext<CommandSourceStack> context = parseResults.getContext().build(parseResults.getReader().getString());

        String command = parseResults.getReader().getString().split(" ")[0];

        if (command.equals("tp") || command.equals("teleport")) {
            CommandSourceStack source = context.getSource();

            try {
                Vec3 targetPos = getTargetPosition(context, source);
                ServerLevel serverLevel = (ServerLevel) source.getLevel();
                if (targetPos != null && serverLevel != null) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL, targetPos.x, targetPos.y, targetPos.z, 50, 0.5, 1.0, 0.5, 0.1);
                }
            } catch (Exception e) {
                //
            }
        }
    }

    private static Vec3 getTargetPosition(CommandContext<CommandSourceStack> context, CommandSourceStack source) {
        try {
            Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
            if (!entities.isEmpty()) {
                Entity target = entities.iterator().next();
                return target.position();
            }
        } catch (IllegalArgumentException e) {
            //
        } catch (CommandSyntaxException e) {
            //
        }

        try {
            Coordinates coords = Vec3Argument.getCoordinates(context, "location");
            return coords.getPosition(source);
        } catch (IllegalArgumentException e) {
            //
        }

        return null;
    }

}
