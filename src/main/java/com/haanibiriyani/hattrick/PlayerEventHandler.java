package com.haanibiriyani.hattrick;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID)
public class PlayerEventHandler {

    // Store the hat BEFORE the player dies
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            ItemStack helmet = player.getInventory().getArmor(3);

            // Check if it's the Hat Man's Hat
            if (isHatMansHat(helmet)) {
                // Store the hat in the player's persistent data
                CompoundTag hatData = helmet.save(new CompoundTag());
                player.getPersistentData().put("HatToKeepOnDeath", hatData);

                // Remove the hat from the player's inventory to prevent it from dropping
                // We don't actually remove it, we just mark it to be prevented from dropping
                player.getPersistentData().putBoolean("PreventHatDrop", true);
            }
        }
    }

    // Prevent the hat from dropping
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if we need to prevent hat drops
            if (player.getPersistentData().getBoolean("PreventHatDrop")) {
                // Remove any drops that are Hat Man's Hat
                event.getDrops().removeIf(entityItem -> {
                    ItemStack stack = entityItem.getItem();
                    return isHatMansHat(stack);
                });

                // Clear the flag
                player.getPersistentData().remove("PreventHatDrop");
            }
        }
    }

    // Restore the hat after respawn
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            CompoundTag data = player.getPersistentData();

            if (data.contains("HatToKeepOnDeath")) {
                // Small delay to ensure inventory is ready
                player.level().getServer().execute(() -> {
                    // Restore the hat to helmet slot
                    CompoundTag hatData = data.getCompound("HatToKeepOnDeath");
                    ItemStack hat = ItemStack.of(hatData);
                    player.getInventory().armor.set(3, hat);

                    // Remove the stored data
                    data.remove("HatToKeepOnDeath");
                });
            }
        }
    }

    // Also handle clone event to copy persistent data
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // Copy persistent data from old player to new player
            CompoundTag oldData = event.getOriginal().getPersistentData();
            CompoundTag newData = event.getEntity().getPersistentData();

            // Copy over our hat data if it exists
            if (oldData.contains("HatToKeepOnDeath")) {
                newData.put("HatToKeepOnDeath", oldData.get("HatToKeepOnDeath").copy());
            }

            // Copy transformation state
            if (oldData.contains("HatManTransformed")) {
                newData.putBoolean("HatManTransformed", oldData.getBoolean("HatManTransformed"));
            }

            // Reset tab list hidden flag so it gets re-evaluated
            newData.putBoolean("TabListHidden", false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up any transformation effects when player logs out
        Player player = event.getEntity();
        player.getPersistentData().putBoolean("TabListHidden", false);
    }

    private static boolean isHatMansHat(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("HatMansHat");
    }
}