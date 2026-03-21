package com.haanibiriyani.hattrick.mixin;

import com.haanibiriyani.hattrick.client.HatManClientHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    private static final ResourceLocation HAT_MAN_SKIN =
            new ResourceLocation("hattrick", "textures/skins/hat_man.png");

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void onGetSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer)(Object) this;
        if (HatManClientHandler.isTransformed(player.getUUID())) {
            cir.setReturnValue(HAT_MAN_SKIN);
        }
    }
}