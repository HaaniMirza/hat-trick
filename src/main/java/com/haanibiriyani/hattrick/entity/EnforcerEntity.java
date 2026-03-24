package com.haanibiriyani.hattrick.entity;

import com.haanibiriyani.hattrick.ModItems;
import com.haanibiriyani.hattrick.ModSounds;
import com.haanibiriyani.hattrick.entity.ai.EnforcerAggroManager;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
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

import java.util.*;

public class EnforcerEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> AGGRESSIVE =
            SynchedEntityData.defineId(EnforcerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_SUMMON =
            SynchedEntityData.defineId(EnforcerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RETALIATING =
            SynchedEntityData.defineId(EnforcerEntity.class, EntityDataSerializers.BOOLEAN);

    // AGGRO LIST: replaces single targetPlayerUUID
    private final Set<UUID> aggroTargets = new HashSet<>();

    private int aggroCheckCooldown = 0;
    private boolean hasReinforcedHalf = false;
    private boolean hasReinforcedQuarter = false;

    private int aggroSoundCooldown   = 0;
    private int observeSoundCooldown = 0;
    private int idleSoundCooldown    = 0;
    private boolean wasInWarningState = false;

    public EnforcerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(2.0F);
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
        EnforcerAggroManager.clearWarning(this);
    }

    private void updateAttackDamage(ServerLevel level) {
        double scaledDamage = com.haanibiriyani.hattrick.EnforcerTracker.calculateAttackDamage(level);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(scaledDamage);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AGGRESSIVE, false);
        this.entityData.define(CAN_SUMMON, true);
        this.entityData.define(RETALIATING, false);
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
            if (canSummon()) {
                float healthPercentage = this.getHealth() / this.getMaxHealth();
                if (this.tickCount % 20 == 0) {
                    System.out.println("Enforcer health: " + this.getHealth() + "/" + this.getMaxHealth() + " (" + (healthPercentage * 100) + "%)");
                    System.out.println("Can summon: " + canSummon() + ", HasReinforcedHalf: " + hasReinforcedHalf + ", HasReinforcedQuarter: " + hasReinforcedQuarter);
                }
                checkAndSummonReinforcements();
            }

            if (aggroCheckCooldown > 0) {
                aggroCheckCooldown--;
            } else {
                aggroCheckCooldown = 20;
                checkAggroConditions();

                if (this.level() instanceof ServerLevel serverLevel) {
                    updateAttackDamage(serverLevel);
                }

                if (isAggressive() || getTarget() != null) {
                    alertNearbyEnforcers();
                }
            }

            if (isAggressive() || getTarget() != null) {
                applyDebuffsToNearbyPlayers();
            }

            tickSounds();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.ENFORCER_DAMAGE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

            // AGGRO LIST: add only the attacking player to the aggro set
            if (source.getEntity() instanceof Player attacker) {
                aggroTargets.add(attacker.getUUID());
                this.setRetaliating(true);
                this.setAggressive(true);
                this.setTarget(attacker);
            }
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.ENFORCER_DEATH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    private void tickSounds() {
        boolean aggressive = isAggressive() || getTarget() != null;
        boolean inWarning  = EnforcerAggroManager.isInWarningState(this);

        if (inWarning && !wasInWarningState) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.ENFORCER_WARN.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        wasInWarningState = inWarning;

        boolean isObserving = !aggressive && !inWarning &&
                !this.level().getEntitiesOfClass(Player.class,
                        this.getBoundingBox().inflate(35.0D),
                        player -> !player.isSpectator() && this.hasLineOfSight(player)
                ).isEmpty();

        if (aggressive) {
            if (aggroSoundCooldown > 0) {
                aggroSoundCooldown--;
            } else {
                aggroSoundCooldown = 100 + this.random.nextInt(40);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.ENFORCER_AGGRO.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            }
            observeSoundCooldown = 0;
            idleSoundCooldown    = 0;

        } else if (isObserving) {
            if (observeSoundCooldown > 0) {
                observeSoundCooldown--;
            } else {
                observeSoundCooldown = 60 + this.random.nextInt(40);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.ENFORCER_OBSERVE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            }
            aggroSoundCooldown = 0;
            idleSoundCooldown  = 0;

        } else if (inWarning) {
            aggroSoundCooldown   = 0;
            observeSoundCooldown = 0;
            idleSoundCooldown    = 0;

        } else {
            if (idleSoundCooldown > 0) {
                idleSoundCooldown--;
            } else {
                idleSoundCooldown = 200 + this.random.nextInt(200);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.ENFORCER_IDLE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            }
            aggroSoundCooldown   = 0;
            observeSoundCooldown = 0;
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    private void checkAggroConditions() {
        // AGGRO LIST: de-escalate if no target and no remaining aggro targets are alive nearby
        if (this.getTarget() == null && isAggressive()) {
            pruneDeadOrGoneTargets();

            if (aggroTargets.isEmpty() && !isRetaliating()) {
                setAggressive(false);
                return;
            }
        }

        if (this.getTarget() != null) return;

        // AGGRO LIST: check group aggro — add all group members to aggro set
        List<Player> groupTargets = EnforcerAggroManager.getAggroTargetsFromGroup(this);
        if (!groupTargets.isEmpty()) {
            for (Player p : groupTargets) {
                aggroTargets.add(p.getUUID());
            }
            setAggressive(true);
            // Target the closest
            groupTargets.stream()
                    .min(Comparator.comparingDouble(this::distanceToSqr))
                    .ifPresent(this::setTarget);
        }
    }

    /**
     * Removes UUIDs from aggroTargets if that player is no longer alive or
     * within follow range. Called during de-escalation checks.
     */
    // AGGRO LIST: cleans up stale targets
    private void pruneDeadOrGoneTargets() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        aggroTargets.removeIf(uuid -> {
            Player player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            // Remove if offline, dead, or out of a generous tracking range
            return player == null || !player.isAlive() ||
                    this.distanceToSqr(player) > 60.0D * 60.0D;
        });
    }

    private void applyDebuffsToNearbyPlayers() {
        final double DEBUFF_RANGE = 5.0D;
        final int EFFECT_DURATION = 60;

        // AGGRO LIST: only debuff players in the aggro target list
        java.util.List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(DEBUFF_RANGE),
                player -> !player.isSpectator() && !player.isCreative()
                        && aggroTargets.contains(player.getUUID())
        );

        for (Player player : nearbyPlayers) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WEAKNESS,
                    EFFECT_DURATION, 2, false, false, true));

            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    EFFECT_DURATION, 3, false, false, true));
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
            enforcer.setAggressive(true);
            enforcer.setRetaliating(false);
            // AGGRO LIST: propagate our aggro target list to alerted enforcers
            enforcer.aggroTargets.addAll(this.aggroTargets);

            if (myTarget != null) {
                enforcer.setTarget(myTarget);
            }
        }
    }

    private boolean shouldAttackPlayer(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        // AGGRO LIST: only attack players in the aggro set
        if (isAggressive() && aggroTargets.contains(player.getUUID())) {
            return true;
        }

        return false;
    }

    // AGGRO LIST: public accessors for use in TimeoutEventHandler and summonReinforcements
    public Set<UUID> getAggroTargets() {
        return Collections.unmodifiableSet(aggroTargets);
    }

    public void addAggroTarget(UUID uuid) {
        aggroTargets.add(uuid);
    }

    public void clearAggroTargets() {
        aggroTargets.clear();
    }

    public void setAggressive(boolean aggressive) {
        this.entityData.set(AGGRESSIVE, aggressive);
        if (!aggressive) {
            this.entityData.set(RETALIATING, false);
            aggroTargets.clear();
        }
    }

    public boolean isAggressive() {
        return this.entityData.get(AGGRESSIVE);
    }

    public void setRetaliating(boolean retaliating) {
        this.entityData.set(RETALIATING, retaliating);
    }

    public boolean isRetaliating() {
        return this.entityData.get(RETALIATING);
    }

    public void setCanSummon(boolean canSummon) {
        this.entityData.set(CAN_SUMMON, canSummon);
    }

    public boolean canSummon() {
        return this.entityData.get(CAN_SUMMON);
    }

    private void checkAndSummonReinforcements() {
        if (!isAggressive() && getTarget() == null) return;

        float healthPercentage = this.getHealth() / this.getMaxHealth();

        if (!hasReinforcedHalf && healthPercentage <= 0.5f) {
            hasReinforcedHalf = true;
            System.out.println("Enforcer summoning reinforcements at 50% health!");
            summonReinforcements(2);
        }

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

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.ENFORCER_SUMMON.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

        for (int i = 0; i < count; i++) {
            EnforcerEntity reinforcement = ModEntities.ENFORCER.get().create(serverLevel);
            if (reinforcement != null) {
                double angle    = (Math.PI * 2 * i) / count;
                double distance = 2.0D;
                double x        = this.getX() + Math.cos(angle) * distance;
                double z        = this.getZ() + Math.sin(angle) * distance;

                reinforcement.moveTo(x, this.getY(), z, this.random.nextFloat() * 360.0F, 0.0F);
                reinforcement.setCanSummon(false);
                reinforcement.setAggressive(this.isAggressive());
                reinforcement.setRetaliating(false);
                // AGGRO LIST: propagate aggro targets to reinforcements
                reinforcement.aggroTargets.addAll(this.aggroTargets);

                if (this.getTarget() != null) {
                    reinforcement.setTarget(this.getTarget());
                }

                serverLevel.addFreshEntity(reinforcement);
                System.out.println("Spawned reinforcement at " + x + ", " + this.getY() + ", " + z);

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

        if (canSummon() && !this.level().isClientSide) {
            ItemStack fiber = new ItemStack(ModItems.ENFORCED_FIBER.get());
            fiber.enchant(net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE, 1);
            this.spawnAtLocation(fiber);
        }
    }
}