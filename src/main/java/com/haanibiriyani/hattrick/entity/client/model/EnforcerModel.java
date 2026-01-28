package com.haanibiriyani.hattrick.entity.client.model;

import com.haanibiriyani.hattrick.entity.EnforcerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class EnforcerModel extends EntityModel<EnforcerEntity> {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tendrils;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public EnforcerModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tendrils = root.getChild("tendrils");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.right_leg = root.getChild("right_leg");
		this.left_leg = root.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(24, 16).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 0.0F));

		PartDefinition tendrils = partdefinition.addOrReplaceChild("tendrils", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -8.5F, 4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 0.0F));

		PartDefinition tendril_l3_r1 = tendrils.addOrReplaceChild("tendril_l3_r1", CubeListBuilder.create().texOffs(48, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.382F, -7.5656F, 12.4108F, -0.263F, -0.2448F, -0.1338F));

		PartDefinition tendril_l2_r1 = tendrils.addOrReplaceChild("tendril_l2_r1", CubeListBuilder.create().texOffs(32, 12).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.988F, -7.3888F, 10.1013F, 0.2606F, -0.2448F, -0.1338F));

		PartDefinition tendril_l1_r1 = tendrils.addOrReplaceChild("tendril_l1_r1", CubeListBuilder.create().texOffs(48, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -6.0F, 5.0F, 0.2618F, 0.2618F, 0.0F));

		PartDefinition tendril_m3_r1 = tendrils.addOrReplaceChild("tendril_m3_r1", CubeListBuilder.create().texOffs(48, 0).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -5.8804F, 11.7713F, 0.2618F, 0.0F, 0.0F));

		PartDefinition tendril_m2_r1 = tendrils.addOrReplaceChild("tendril_m2_r1", CubeListBuilder.create().texOffs(32, 12).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -7.7066F, 9.3912F, -0.6545F, 0.0F, 0.0F));

		PartDefinition tendril_r3_r1 = tendrils.addOrReplaceChild("tendril_r3_r1", CubeListBuilder.create().texOffs(48, 0).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5749F, -7.9839F, 11.9751F, -0.263F, 0.2448F, 0.1338F));

		PartDefinition tendril_r2_r1 = tendrils.addOrReplaceChild("tendril_r2_r1", CubeListBuilder.create().texOffs(32, 12).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.25F, -7.2941F, 9.6651F, 0.2606F, 0.2448F, 0.1338F));

		PartDefinition tendril_r1_r1 = tendrils.addOrReplaceChild("tendril_r1_r1", CubeListBuilder.create().texOffs(48, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -6.0F, 5.0F, 0.2618F, -0.2618F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 22.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(32, 0).addBox(-2.0F, 20.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -13.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 22.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(32, 0).addBox(-2.0F, 20.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -13.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 25.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -1.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(24, 28).addBox(3.0F, 0.0F, -1.0F, 2.0F, 25.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -1.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(EnforcerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Head rotation based on where entity is looking
		this.head.yRot = netHeadYaw * 0.017453292F;
		this.head.xRot = headPitch * 0.017453292F;

		// Tendrils follow head rotation
		this.tendrils.yRot = this.head.yRot;
		this.tendrils.xRot = this.head.xRot;

		// Enderman-style walking animation
		this.right_leg.xRot = (float) (Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount);
		this.left_leg.xRot = (float) (Math.cos(limbSwing * 0.6662F + Math.PI) * 1.4F * limbSwingAmount);

		// Enderman-style arm movement - arms raised and swinging
		this.right_arm.xRot = (float) (Math.cos(limbSwing * 0.6662F + Math.PI) * 2.0F * limbSwingAmount * 0.5F - Math.PI / 10.0F);
		this.left_arm.xRot = (float) (Math.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F - Math.PI / 10.0F);

		// Slight arm spread for menacing pose
		this.right_arm.zRot = 0.05F;
		this.left_arm.zRot = -0.05F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		tendrils.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}