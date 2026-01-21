package com.haanibiriyani.hattrick.block.menu;

import com.haanibiriyani.hattrick.block.entity.EnforcementBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EnforcementBlockMenu extends AbstractContainerMenu {
    private final EnforcementBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public EnforcementBlockMenu(int containerId, Inventory playerInventory, EnforcementBlockEntity blockEntity) {
        super(ModMenuTypes.ENFORCEMENT_BLOCK.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public EnforcementBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isCreative() && stillValid(access, player, com.haanibiriyani.hattrick.ModBlocks.ENFORCEMENT_BLOCK.get());
    }
}