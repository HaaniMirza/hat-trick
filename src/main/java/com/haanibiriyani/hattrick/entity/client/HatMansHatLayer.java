package com.haanibiriyani.hattrick.entity.client;

import com.haanibiriyani.hattrick.entity.client.model.HatMansHatModel;
import com.haanibiriyani.hattrick.item.HatMansHatItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class HatMansHatLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation HAT_TEXTURE =
            new ResourceLocation("hattrick", "textures/models/armor/hat_mans_hat_layer_1.png");

    private final HatMansHatModel model;

    public HatMansHatLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            EntityModelSet modelSet) {
        super(parent);
        this.model = new HatMansHatModel(modelSet.bakeLayer(ModModelLayers.HAT_MAN_HAT_LAYER));
    }

    @Override
    public void render(PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight,
                       AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        ItemStack helmet = player.getInventory().getArmor(3);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof HatMansHatItem)) return;

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);

        poseStack.scale(0.675F, 0.675F, 0.675F);
        poseStack.translate(0, -0.1F, 0);

        model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityCutoutNoCull(HAT_TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1F, 1F, 1F, 1F
        );

        poseStack.popPose();
    }
}