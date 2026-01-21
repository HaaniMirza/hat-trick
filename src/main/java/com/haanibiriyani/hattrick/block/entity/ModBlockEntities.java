package com.haanibiriyani.hattrick.block.entity;

import com.haanibiriyani.hattrick.HatTrickMod;
import com.haanibiriyani.hattrick.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HatTrickMod.MODID);

    public static final RegistryObject<BlockEntityType<EnforcementBlockEntity>> ENFORCEMENT_BLOCK =
            BLOCK_ENTITIES.register("enforcement_block",
                    () -> BlockEntityType.Builder.of(
                            EnforcementBlockEntity::new,
                            ModBlocks.ENFORCEMENT_BLOCK.get()
                    ).build(null));
}