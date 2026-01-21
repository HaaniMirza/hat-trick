package com.haanibiriyani.hattrick.entity.ai;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class StalkPlayerGoal extends Goal {
    private final EnforcerEntity enforcer;
    private Player targetPlayer;
    private static final double DESIRED_DISTANCE = 10.0D;
    private static final double DETECTION_RANGE = 35.0D;
    private static final double TOLERANCE = 1.5D; // Distance tolerance before moving
    private int recalculatePathTimer;

    public StalkPlayerGoal(EnforcerEntity enforcer) {
        this.enforcer = enforcer;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Don't stalk if aggressive or has a target
        if (this.enforcer.isAggressive() || this.enforcer.getTarget() != null) {
            return false;
        }

        // Find nearest visible player
        List<Player> nearbyPlayers = this.enforcer.level().getEntitiesOfClass(
                Player.class,
                this.enforcer.getBoundingBox().inflate(DETECTION_RANGE),
                player -> !player.isSpectator() && !player.isCreative() && this.enforcer.hasLineOfSight(player)
        );

        if (nearbyPlayers.isEmpty()) {
            return false;
        }

        // Target the closest player
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player player : nearbyPlayers) {
            double distance = this.enforcer.distanceToSqr(player);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = player;
            }
        }

        this.targetPlayer = closest;
        return this.targetPlayer != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }

        // Stop stalking if aggressive or has combat target
        if (this.enforcer.isAggressive() || this.enforcer.getTarget() != null) {
            return false;
        }

        // Stop if player is too far or out of sight
        double distance = this.enforcer.distanceToSqr(this.targetPlayer);
        if (distance > DETECTION_RANGE * DETECTION_RANGE || !this.enforcer.hasLineOfSight(this.targetPlayer)) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        this.recalculatePathTimer = 0;
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        this.enforcer.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) {
            return;
        }

        // Always look at the player
        this.enforcer.getLookControl().setLookAt(
                this.targetPlayer,
                30.0F,
                30.0F
        );

        double currentDistance = this.enforcer.distanceTo(this.targetPlayer);

        // Recalculate path periodically
        if (--this.recalculatePathTimer <= 0) {
            this.recalculatePathTimer = 10; // Recalculate every 10 ticks (0.5 seconds)

            if (Math.abs(currentDistance - DESIRED_DISTANCE) > TOLERANCE) {
                Vec3 targetPos;

                if (currentDistance < DESIRED_DISTANCE) {
                    // Too close - move away
                    targetPos = calculateRetreatPosition();
                } else {
                    // Too far - move closer
                    targetPos = calculateApproachPosition();
                }

                if (targetPos != null) {
                    this.enforcer.getNavigation().moveTo(
                            targetPos.x,
                            targetPos.y,
                            targetPos.z,
                            1.2D // Move at 120% speed
                    );
                }
            } else {
                // At correct distance, stop moving
                this.enforcer.getNavigation().stop();
            }
        }
    }

    private Vec3 calculateRetreatPosition() {
        // Calculate position away from player
        Vec3 enforcerPos = this.enforcer.position();
        Vec3 playerPos = this.targetPlayer.position();

        Vec3 direction = enforcerPos.subtract(playerPos).normalize();
        Vec3 retreatPos = playerPos.add(direction.scale(DESIRED_DISTANCE));

        return retreatPos;
    }

    private Vec3 calculateApproachPosition() {
        // Calculate position closer to player
        Vec3 enforcerPos = this.enforcer.position();
        Vec3 playerPos = this.targetPlayer.position();

        Vec3 direction = playerPos.subtract(enforcerPos).normalize();
        Vec3 approachPos = enforcerPos.add(direction.scale(this.enforcer.distanceTo(this.targetPlayer) - DESIRED_DISTANCE));

        return approachPos;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true; // Update every tick for smooth looking
    }
}