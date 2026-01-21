package com.haanibiriyani.hattrick.block.menu;

import com.haanibiriyani.hattrick.HatTrickMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, HatTrickMod.MODID);

    public static final RegistryObject<MenuType<EnforcementBlockMenu>> ENFORCEMENT_BLOCK =
            MENUS.register("enforcement_block",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        net.minecraft.core.BlockPos pos = data.readBlockPos();
                        net.minecraft.world.level.block.entity.BlockEntity be = inv.player.level().getBlockEntity(pos);
                        if (be instanceof com.haanibiriyani.hattrick.block.entity.EnforcementBlockEntity enforcementBlock) {
                            return new EnforcementBlockMenu(windowId, inv, enforcementBlock);
                        }
                        throw new IllegalStateException("Invalid block entity at " + pos);
                    }));
}