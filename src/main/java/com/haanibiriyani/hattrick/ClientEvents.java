package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.entity.ModEntities;
import com.haanibiriyani.hattrick.entity.client.EnforcerRenderer;
import com.haanibiriyani.hattrick.entity.client.ModModelLayers;
import com.haanibiriyani.hattrick.entity.client.model.EnforcerModel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.ENFORCER.get(), EnforcerRenderer::new);

            net.minecraft.client.gui.screens.MenuScreens.register(
                    com.haanibiriyani.hattrick.block.menu.ModMenuTypes.ENFORCEMENT_BLOCK.get(),
                    com.haanibiriyani.hattrick.block.screen.EnforcementBlockScreen::new
            );
        });
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.ENFORCER_LAYER, EnforcerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.TRANSFORM_KEY);
    }
}