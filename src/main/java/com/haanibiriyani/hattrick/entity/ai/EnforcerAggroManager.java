package com.haanibiriyani.hattrick.entity.ai;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class EnforcerAggroManager {
    private static final double GROUP_DETECTION_RANGE = 16.0D;
    private static final long WARN_DURATION_TICKS = 200L;

    private static int minGroupSize = 4;

    private static final Map<UUID, Long> warningStartTimes = new HashMap<>();

    public static int getMinGroupSize() {
        return minGroupSize;
    }

    public static double getGroupDetectionRange() {
        return GROUP_DETECTION_RANGE;
    }

    public static void setMinGroupSize(int size) {
        minGroupSize = Math.max(1, size);
        warningStartTimes.clear();
    }

    /**
     * Checks whether the warning timer has expired for this enforcer while a
     * qualifying group is still present. Returns the list of players to aggro
     * against, or an empty list if not yet ready to aggro.
     */
    public static List<Player> getAggroTargetsFromGroup(EnforcerEntity enforcer) {
        if (enforcer.isAggressive()) {
            warningStartTimes.remove(enforcer.getUUID());
            return Collections.emptyList();
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
                return Collections.emptyList();
            }

            long elapsed = currentTime - warningStartTimes.get(enforcerID);

            if (elapsed > WARN_DURATION_TICKS) {
                warningStartTimes.remove(enforcerID);
                return nearbyPlayers; // Return all group members
            }

            return Collections.emptyList();

        } else {
            warningStartTimes.remove(enforcerID);
            return Collections.emptyList();
        }
    }

    // Kept for backward compatibility with isInPlayerGroup checks
    public static boolean shouldAggroOnPlayerGroup(EnforcerEntity enforcer) {
        List<Player> targets = getAggroTargetsFromGroup(enforcer);
        if (!targets.isEmpty()) {
            Player closest = targets.stream()
                    .min(Comparator.comparingDouble(enforcer::distanceToSqr))
                    .orElse(null);
            if (closest != null) {
                enforcer.setTarget(closest);
                return true;
            }
        }
        return false;
    }

    public static boolean isInWarningState(EnforcerEntity enforcer) {
        return warningStartTimes.containsKey(enforcer.getUUID());
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