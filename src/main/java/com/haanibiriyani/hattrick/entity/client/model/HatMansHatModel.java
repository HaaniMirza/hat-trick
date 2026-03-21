package com.haanibiriyani.hattrick.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class HatMansHatModel extends Model {

    private final ModelPart band;
    private final ModelPart crown;
    private final ModelPart flare;

    public HatMansHatModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.band  = root.getChild("band");
        this.flare = root.getChild("flare");
        this.crown = root.getChild("crown");
    }

    /**
     * Coordinates converted from Blockbench by subtracting the head pivot (8, 24, 8).
     * hat_base brim:  from [1,12,1]  to [15,14,15] → addBox(-7, -12, -7,  14, 2, 14)
     * hat_base crown: from [2,15,2]  to [14,23,14] → addBox(-6,  -9, -6,  12, 8, 12)
     * hat_flare:      from [-4,14,-4] to [20,15,20] → addBox(-12, -10, -12, 24, 1, 24)
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Flare: texOffs(0, 0), box 24×1×24
        root.addOrReplaceChild("flare",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-12F, -11F, -12F, 24, 1, 24),
                PartPose.ZERO);

        // Band: texOffs(0, 25), box 14×2×14
        root.addOrReplaceChild("band",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .addBox(-7F, -10F, -7F, 14, 2, 14),
                PartPose.ZERO);

        // Crown: texOffs(0, 41), box 12×8×12
        root.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(0, 41)
                        .addBox(-6F, -19F, -6F, 12, 8, 12),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer,
                               int packedLight, int packedOverlay,
                               float r, float g, float b, float a) {
        band.render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
        flare.render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
        crown.render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
    }
}