package com.haanibiriyani.hattrick.entity;

import com.haanibiriyani.hattrick.HatTrickMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HatTrickMod.MODID);

    public static final RegistryObject<EntityType<EnforcerEntity>> ENFORCER = ENTITIES.register("enforcer",
            () -> EntityType.Builder.of(EnforcerEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(8)
                    .build("enforcer"));
}