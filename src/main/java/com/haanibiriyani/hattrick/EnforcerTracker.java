package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID)
public class EnforcerTracker {
    // Track enforcers per dimension
    private static final Map<ServerLevel, Map<UUID, EnforcerEntity>> enforcersByLevel = new WeakHashMap<>();
    private static int updateTimer = 0;

    /**
     * Calculate attack damage based on number of loaded enforcers
     * Formula: 0.5 + (count - 1) * 1.0
     * 1 enforcer = 0.5 hearts (1.0 damage)
     * 2 enforcers = 1.5 hearts (3.0 damage)
     * 3 enforcers = 2.5 hearts (5.0 damage)
     * etc.
     */
    public static double calculateAttackDamage(ServerLevel level) {
        int count = getEnforcerCount(level);
        if (count <= 0) return 1.0D; // Fallback

        return 1.0D + ((count - 1) * 2.0D);
    }

    public static int getEnforcerCount(ServerLevel level) {
        Map<UUID, EnforcerEntity> enforcers = enforcersByLevel.get(level);
        if (enforcers == null) return 0;

        // Clean up dead/removed enforcers
        enforcers.entrySet().removeIf(entry -> {
            EnforcerEntity enforcer = entry.getValue();
            return enforcer == null || !enforcer.isAlive() || enforcer.isRemoved();
        });

        return enforcers.size();
    }

    public static void registerEnforcer(EnforcerEntity enforcer) {
        if (enforcer.level() instanceof ServerLevel serverLevel) {
            enforcersByLevel.computeIfAbsent(serverLevel, k -> new HashMap<>())
                    .put(enforcer.getUUID(), enforcer);
        }
    }

    public static void unregisterEnforcer(EnforcerEntity enforcer) {
        if (enforcer.level() instanceof ServerLevel serverLevel) {
            Map<UUID, EnforcerEntity> enforcers = enforcersByLevel.get(serverLevel);
            if (enforcers != null) {
                enforcers.remove(enforcer.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Periodically clean up dead enforcers
            if (++updateTimer >= 100) { // Every 5 seconds
                updateTimer = 0;
                for (Map<UUID, EnforcerEntity> enforcers : enforcersByLevel.values()) {
                    enforcers.entrySet().removeIf(entry -> {
                        EnforcerEntity enforcer = entry.getValue();
                        return enforcer == null || !enforcer.isAlive() || enforcer.isRemoved();
                    });
                }
            }
        }
    }
}