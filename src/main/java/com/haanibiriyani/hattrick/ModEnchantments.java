package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.enchantment.CurseOfMidasEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, HatTrickMod.MODID);

    public static final RegistryObject<Enchantment> CURSE_OF_MIDAS = ENCHANTMENTS.register("curse_of_midas",
            CurseOfMidasEnchantment::new);
}