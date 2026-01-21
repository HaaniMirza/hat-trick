package com.haanibiriyani.hattrick.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MidasPickaxeItem extends PickaxeItem {

    public MidasPickaxeItem(Properties properties) {
        super(Tiers.NETHERITE, 1, -2.8f, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        // The actual drop conversion happens in MidasPickaxeEventHandler
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false; // Indestructible
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // Cannot be enchanted
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}