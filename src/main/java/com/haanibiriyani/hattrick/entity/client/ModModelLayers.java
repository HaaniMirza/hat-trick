package com.haanibiriyani.hattrick.entity.client;

import com.haanibiriyani.hattrick.HatTrickMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation ENFORCER_LAYER = new ModelLayerLocation(
            new ResourceLocation(HatTrickMod.MODID, "enforcer"),
            "main"
    );
}