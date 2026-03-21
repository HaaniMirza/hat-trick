package com.haanibiriyani.hattrick.entity.ai;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.*;

public class EnforcerAggroManager {
    private static final double GROUP_DETECTION_RANGE = 16.0D;
    private static final long WARN_DURATION_TICKS = 200L;

    private static int minGroupSize = 4;

    private static final Map<UUID, Long> warningStartTimes = new HashMap<>();

    public static int getMinGroupSize() {
        return minGroupSize;
    }

    /**
     * Sets the minimum group size that triggers the warning/aggro sequence.
     * Clamped to a minimum of 1 to prevent nonsensical values.
     */
    public static void setMinGroupSize(int size) {
        minGroupSize = Math.max(1, size);
        // Clear all active warnings since the threshold has changed
        warningStartTimes.clear();
    }

    public static boolean shouldAggroOnPlayerGroup(EnforcerEntity enforcer) {
        if (enforcer.isAggressive()) {
            warningStartTimes.remove(enforcer.getUUID());
            return false; // Already aggressive
        }

        AABB searchBox = enforcer.getBoundingBox().inflate(GROUP_DETECTION_RANGE);
        List<Player> nearbyPlayers = enforcer.level().getEntitiesOfClass(
                Player.class,
                searchBox,
                player -> !player.isSpectator() && enforcer.hasLineOfSight(player)
        );

        UUID enforcerID = enforcer.getUUID();

        if (nearbyPlayers.size() >= minGroupSize) {
            long currentTime = enforcer.level().getGameTime();

            if (!warningStartTimes.containsKey(enforcerID)) {
                warningStartTimes.put(enforcerID, currentTime);
                return false;
            }

            long elapsed = currentTime - warningStartTimes.get(enforcerID);

            if (elapsed > WARN_DURATION_TICKS) {
                warningStartTimes.remove(enforcerID);
                Player closestPlayer = nearbyPlayers.stream()
                        .min(Comparator.comparingDouble(enforcer::distanceToSqr))
                        .orElse(null);

                if (closestPlayer != null) {
                    enforcer.setTarget(closestPlayer);
                    return true;
                }
            }

            return false;

        } else {

            return false;

        }
    }

    public static boolean isInWarningState(EnforcerEntity enforcer) {
        return warningStartTimes.containsKey(enforcer.getUUID());
    }

    public static double getGroupDetectionRange() {
        return GROUP_DETECTION_RANGE;
    }

    public static float getWarningProgress(EnforcerEntity enforcer) {
        Long startTime = warningStartTimes.get(enforcer.getUUID());
        if (startTime == null) return 0.0f;
        long elapsed = enforcer.level().getGameTime() - startTime;
        return Math.min(1.0f, (float) elapsed / WARN_DURATION_TICKS);
    }

    public static void clearWarning(EnforcerEntity enforcer) {
        warningStartTimes.remove(enforcer.getUUID());
    }

    public static boolean isInPlayerGroup(Player player, EnforcerEntity enforcer) {
        AABB searchBox = player.getBoundingBox().inflate(GROUP_DETECTION_RANGE);
        List<Player> nearbyPlayers = enforcer.level().getEntitiesOfClass(
                Player.class,
                searchBox,
                p -> !p.isSpectator() && p != player && enforcer.hasLineOfSight(p)
        );

        return nearbyPlayers.size() >= (minGroupSize - 1);
    }
}