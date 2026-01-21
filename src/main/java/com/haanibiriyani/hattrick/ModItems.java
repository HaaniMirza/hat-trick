package com.haanibiriyani.hattrick;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HatTrickMod.MODID);

    public static final RegistryObject<Item> ENFORCED_FIBER = ITEMS.register("enforced_fiber",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> HAT_MANS_HAT = ITEMS.register("hat_mans_hat",
            () -> new com.haanibiriyani.hattrick.item.HatMansHatItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> MIDAS_PICKAXE = ITEMS.register("midas_pickaxe",
            () -> new com.haanibiriyani.hattrick.item.MidasPickaxeItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));
}