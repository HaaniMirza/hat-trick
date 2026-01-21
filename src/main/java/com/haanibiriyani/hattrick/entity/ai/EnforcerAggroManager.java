package com.haanibiriyani.hattrick.entity.ai;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EnforcerAggroManager {
    private static final double GROUP_DETECTION_RANGE = 16.0D;
    private static final int MIN_GROUP_SIZE = 3;

    public static boolean shouldAggroOnPlayerGroup(EnforcerEntity enforcer) {
        if (enforcer.isAggressive()) {
            return false; // Already aggressive
        }

        AABB searchBox = enforcer.getBoundingBox().inflate(GROUP_DETECTION_RANGE);
        List<Player> nearbyPlayers = enforcer.level().getEntitiesOfClass(
                Player.class,
                searchBox,
                player -> !player.isSpectator() && enforcer.hasLineOfSight(player)
        );

        if (nearbyPlayers.size() >= MIN_GROUP_SIZE) {
            // Target the closest player in the group
            Player closestPlayer = null;
            double closestDistance = Double.MAX_VALUE;

            for (Player player : nearbyPlayers) {
                double distance = enforcer.distanceToSqr(player);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = player;
                }
            }

            if (closestPlayer != null) {
                enforcer.setTarget(closestPlayer);
                return true;
            }
        }

        return false;
    }

    public static boolean isInPlayerGroup(Player player, EnforcerEntity enforcer) {
        AABB searchBox = player.getBoundingBox().inflate(GROUP_DETECTION_RANGE);
        List<Player> nearbyPlayers = enforcer.level().getEntitiesOfClass(
                Player.class,
                searchBox,
                p -> !p.isSpectator() && p != player && enforcer.hasLineOfSight(p)
        );

        return nearbyPlayers.size() >= (MIN_GROUP_SIZE - 1);
    }
}