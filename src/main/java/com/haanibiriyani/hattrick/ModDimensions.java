package com.haanibiriyani.hattrick;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ModDimensions {
    public static final ResourceKey<Level> TIMEOUT_ABYSS_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation("hattrick", "timeout_abyss"));
}