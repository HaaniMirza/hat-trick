package com.haanibiriyani.hattrick.entity;

import com.haanibiriyani.hattrick.ModItems;
import com.haanibiriyani.hattrick.entity.ai.EnforcerAggroManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EnforcerEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> AGGRESSIVE =
            SynchedEntityData.defineId(EnforcerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_SUMMON =
            SynchedEntityData.defineId(EnforcerEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID targetPlayerUUID;
    private int aggroCheckCooldown = 0;
    private boolean hasReinforcedHalf = false;
    private boolean hasReinforcedQuarter = false;

    public EnforcerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(2.0F); // Can step up 2 blocks
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.level() instanceof ServerLevel serverLevel) {
            com.haanibiriyani.hattrick.EnforcerTracker.registerEnforcer(this);
            updateAttackDamage(serverLevel);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        com.haanibiriyani.hattrick.EnforcerTracker.unregisterEnforcer(this);
    }

    private void updateAttackDamage(ServerLevel level) {
        double scaledDamage = com.haanibiriyani.hattrick.EnforcerTracker.calculateAttackDamage(level);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(scaledDamage);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D) // Base damage, will be scaled dynamically
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AGGRESSIVE, false);
        this.entityData.define(CAN_SUMMON, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new com.haanibiriyani.hattrick.entity.ai.StalkPlayerGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                this::shouldAttackPlayer));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Check for reinforcement summoning every tick
            if (canSummon()) {
                float healthPercentage = this.getHealth() / this.getMaxHealth();
                // Log every 20 ticks to avoid spam
                if (this.tickCount % 20 == 0) {
                    System.out.println("Enforcer health: " + this.getHealth() + "/" + this.getMaxHealth() + " (" + (healthPercentage * 100) + "%)");
                    System.out.println("Can summon: " + canSummon() + ", HasReinforcedHalf: " + hasReinforcedHalf + ", HasReinforcedQuarter: " + hasReinforcedQuarter);
                }
                checkAndSummonReinforcements();
            }

            if (aggroCheckCooldown > 0) {
                aggroCheckCooldown--;
            } else {
                aggroCheckCooldown = 20; // Check every second
                checkAggroConditions();

                // Update attack damage periodically based on enforcer count
                if (this.level() instanceof ServerLevel serverLevel) {
                    updateAttackDamage(serverLevel);
                }

                // Alert nearby Enforcers if this one is aggressive
                if (isAggressive() || getTarget() != null) {
                    alertNearbyEnforcers();
                }
            }

            // Apply debuffs to nearby players if aggressive
            if (isAggressive() || getTarget() != null) {
                applyDebuffsToNearbyPlayers();
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Never despawn naturally
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true; // Always persist
    }

    private void checkAggroConditions() {
        if (this.getTarget() != null) {
            return; // Already has a target
        }

        // Check for designated target player
        if (targetPlayerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Player targetPlayer = serverLevel.getServer().getPlayerList().getPlayer(targetPlayerUUID);
            if (targetPlayer != null && this.distanceToSqr(targetPlayer) < 35.0D * 35.0D && this.hasLineOfSight(targetPlayer)) {
                this.setTarget(targetPlayer);
                return;
            }
        }

        // Check for group of 3+ players
        if (EnforcerAggroManager.shouldAggroOnPlayerGroup(this)) {
            return; // Target set by manager
        }
    }

    private void applyDebuffsToNearbyPlayers() {
        final double DEBUFF_RANGE = 5.0D;
        final int EFFECT_DURATION = 60; // 3 seconds (20 ticks per second)

        java.util.List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(DEBUFF_RANGE),
                player -> !player.isSpectator() && !player.isCreative()
        );

        for (Player player : nearbyPlayers) {
            // Weakness III (amplifier 2 = level 3)
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WEAKNESS,
                    EFFECT_DURATION,
                    2, // Weakness III
                    false,
                    false,
                    true
            ));

            // Slowness IV (amplifier 3 = level 4)
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    EFFECT_DURATION,
                    3, // Slowness IV
                    false,
                    false,
                    true
            ));
        }
    }

    private void alertNearbyEnforcers() {
        final double ALERT_RANGE = 25.0D;

        java.util.List<EnforcerEntity> nearbyEnforcers = this.level().getEntitiesOfClass(
                EnforcerEntity.class,
                this.getBoundingBox().inflate(ALERT_RANGE),
                enforcer -> enforcer != this && !enforcer.isAggressive() && enforcer.getTarget() == null
        );

        LivingEntity myTarget = this.getTarget();

        for (EnforcerEntity enforcer : nearbyEnforcers) {
            // Make them aggressive
            enforcer.setAggressive(true);

            // If this Enforcer has a target, share it
            if (myTarget != null) {
                enforcer.setTarget(myTarget);
            }
        }
    }

    private boolean shouldAttackPlayer(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        // Always attack if globally aggressive
        if (isAggressive()) {
            return true;
        }

        // Attack designated target
        if (targetPlayerUUID != null && player.getUUID().equals(targetPlayerUUID)) {
            return true;
        }

        // Check for player groups
        return EnforcerAggroManager.isInPlayerGroup(player, this);
    }

    public void setTargetPlayer(UUID playerUUID) {
        this.targetPlayerUUID = playerUUID;
        if (playerUUID == null) {
            this.setTarget(null);
        }
    }

    public UUID getTargetPlayerUUID() {
        return this.targetPlayerUUID;
    }

    public void setAggressive(boolean aggressive) {
        this.entityData.set(AGGRESSIVE, aggressive);
    }

    public boolean isAggressive() {
        return this.entityData.get(AGGRESSIVE);
    }

    public void setCanSummon(boolean canSummon) {
        this.entityData.set(CAN_SUMMON, canSummon);
    }

    public boolean canSummon() {
        return this.entityData.get(CAN_SUMMON);
    }

    private void checkAndSummonReinforcements() {
        // Only summon if aggressive or has a target
        if (!isAggressive() && getTarget() == null) {
            return;
        }

        float healthPercentage = this.getHealth() / this.getMaxHealth();

        // Summon 2 reinforcements at 50% health
        if (!hasReinforcedHalf && healthPercentage <= 0.5f) {
            hasReinforcedHalf = true;
            System.out.println("Enforcer summoning reinforcements at 50% health!");
            summonReinforcements(2);
        }

        // Summon 2 more reinforcements at 25% health
        if (!hasReinforcedQuarter && healthPercentage <= 0.25f) {
            hasReinforcedQuarter = true;
            System.out.println("Enforcer summoning reinforcements at 25% health!");
            summonReinforcements(2);
        }
    }

    private void summonReinforcements(int count) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            System.out.println("Not on server level, cannot summon");
            return;
        }

        System.out.println("Summoning " + count + " reinforcements");

        for (int i = 0; i < count; i++) {
            EnforcerEntity reinforcement = ModEntities.ENFORCER.get().create(serverLevel);
            if (reinforcement != null) {
                // Position around the summoner
                double angle = (Math.PI * 2 * i) / count;
                double distance = 2.0D;
                double x = this.getX() + Math.cos(angle) * distance;
                double z = this.getZ() + Math.sin(angle) * distance;

                reinforcement.moveTo(x, this.getY(), z, this.random.nextFloat() * 360.0F, 0.0F);

                // Reinforcements cannot summon more
                reinforcement.setCanSummon(false);

                // Copy aggressive state and target
                reinforcement.setAggressive(this.isAggressive());
                if (this.getTarget() != null) {
                    reinforcement.setTarget(this.getTarget());
                }

                // Copy target player UUID if set
                if (this.targetPlayerUUID != null) {
                    reinforcement.setTargetPlayer(this.targetPlayerUUID);
                }

                serverLevel.addFreshEntity(reinforcement);
                System.out.println("Spawned reinforcement at " + x + ", " + this.getY() + ", " + z);

                // Spawn particle effect
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.POOF,
                        x, this.getY() + 0.5D, z,
                        20, 0.5D, 0.5D, 0.5D, 0.05D
                );
            } else {
                System.out.println("Failed to create reinforcement entity!");
            }
        }
    }

    @Override
    protected void dropFromLootTable(net.minecraft.world.damagesource.DamageSource damageSource, boolean hitByPlayer) {
        super.dropFromLootTable(damageSource, hitByPlayer);

        // Only drop if this enforcer can summon (i.e., not a reinforcement)
        if (canSummon() && !this.level().isClientSide) {
            ItemStack fiber = new ItemStack(ModItems.ENFORCED_FIBER.get());
            // Add Curse of Vanishing
            fiber.enchant(net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE, 1);
            this.spawnAtLocation(fiber);
        }
    }

}
