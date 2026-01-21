package com.haanibiriyani.hattrick.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class HatMansHatItem extends ArmorItem {

    public HatMansHatItem(Properties properties) {
        super(ArmorMaterials.LEATHER, Type.HELMET, properties);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, net.minecraft.world.entity.player.Player player) {
        super.onCraftedBy(stack, level, player);
        // Add Curse of Binding when crafted
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        // Mark it as a Hat Man's Hat for persistence
        stack.getOrCreateTag().putBoolean("HatMansHat", true);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // Ensure it always has the tag and curse
        if (!stack.hasTag() || !stack.getTag().getBoolean("HatMansHat")) {
            stack.getOrCreateTag().putBoolean("HatMansHat", true);
            if (stack.getEnchantmentLevel(Enchantments.BINDING_CURSE) == 0) {
                stack.enchant(Enchantments.BINDING_CURSE, 1);
            }
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false; // Indestructible
    }
}