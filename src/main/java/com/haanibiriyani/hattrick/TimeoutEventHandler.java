package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hattrick")
public class TimeoutEventHandler {

    private static final net.minecraft.core.particles.SimpleParticleType[] VOID_PARTICLES = {
            net.minecraft.core.particles.ParticleTypes.ASH,
            net.minecraft.core.particles.ParticleTypes.SOUL,
            net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
            net.minecraft.core.particles.ParticleTypes.WARPED_SPORE,
            net.minecraft.core.particles.ParticleTypes.SMOKE,
            net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
            net.minecraft.core.particles.ParticleTypes.END_ROD,
            net.minecraft.core.particles.ParticleTypes.PORTAL,
            net.minecraft.core.particles.ParticleTypes.WITCH,
            net.minecraft.core.particles.ParticleTypes.MYCELIUM
    };

    /**
     * Intercepts any lethal hit on a player.
     * - If inside the Timeout Void, prevent death entirely (health is restored).
     * - If killed by an Enforcer anywhere else, cancel death and send to timeout.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Players cannot die inside the Timeout Void — just restore health
        if (player.level().dimension().equals(ModDimensions.TIMEOUT_ABYSS_LEVEL_KEY)) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
            return;
        }

        if (!isKilledByEnforcer(event.getSource())) return;

        net.minecraft.world.entity.Entity killer = event.getSource().getEntity();
        if (killer instanceof EnforcerEntity enforcer && enforcer.isRetaliating()) {
            enforcer.setRetaliating(false);
            return;
        }

        // Cancel vanilla death (no item drop, no death screen, no respawn)
        event.setCanceled(true);
        TimeoutAbyssManager.sendToTimeout(player);
    }

    /** Drives the timeout countdown. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        TimeoutAbyssManager.tick(event.getServer());

        ServerLevel abyss = event.getServer().getLevel(ModDimensions.TIMEOUT_ABYSS_LEVEL_KEY);
        if (abyss == null) return;

        java.util.Random rand = new java.util.Random();

        for (ServerPlayer player : abyss.players()) {
            // Spawn several particles per tick in a wide radius around each player
            for (int i = 0; i < 8; i++) {
                net.minecraft.core.particles.SimpleParticleType particle =
                        VOID_PARTICLES[rand.nextInt(VOID_PARTICLES.length)];

                double x = player.getX() + (rand.nextDouble() - 0.5D) * 30.0D;
                double y = player.getY() + (rand.nextDouble() - 0.5D) * 10.0D;
                double z = player.getZ() + (rand.nextDouble() - 0.5D) * 30.0D;

                abyss.sendParticles(particle, x, y, z,
                        1,       // count
                        0, 0, 0, // offset (0 so position is exact)
                        0.05D    // speed
                );
            }
        }

    }

    /**
     * If a player in timeout leaves the Timeout Abyss by any means before their
     * timer expires, cancel the remaining time and return their items immediately.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Only care about players leaving the Timeout Abyss
        if (!event.getFrom().equals(ModDimensions.TIMEOUT_ABYSS_LEVEL_KEY)) return;

        // If they're in timeout, return their items right away
        if (player.getServer() != null) {
            TimeoutAbyssManager.returnFromTimeout(player, player.getServer());
        }
    }

    /** Returns offline players whose timer expired while they were away. */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TimeoutAbyssManager.onPlayerLogin(player);
        }
    }

    private static boolean isKilledByEnforcer(DamageSource source) {
        return source.getEntity() instanceof EnforcerEntity ||
                source.getDirectEntity() instanceof EnforcerEntity;
    }



}