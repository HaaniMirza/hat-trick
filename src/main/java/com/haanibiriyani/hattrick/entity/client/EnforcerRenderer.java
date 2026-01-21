package com.haanibiriyani.hattrick.entity.client;

import com.haanibiriyani.hattrick.HatTrickMod;
import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import com.haanibiriyani.hattrick.entity.client.model.EnforcerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EnforcerRenderer extends MobRenderer<EnforcerEntity, EnforcerModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HatTrickMod.MODID, "textures/entity/enforcer.png");

    public EnforcerRenderer(EntityRendererProvider.Context context) {
        super(context, new EnforcerModel(context.bakeLayer(ModModelLayers.ENFORCER_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EnforcerEntity entity) {
        return TEXTURE;
    }
}