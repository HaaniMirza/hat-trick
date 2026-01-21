package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.entity.ModEntities;
import com.haanibiriyani.hattrick.command.HatTrickCommand;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HatTrickMod.MODID)
public class HatTrickMod {
    public static final String MODID = "hattrick";

    public HatTrickMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.registerBlockItems(ModItems.ITEMS);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        com.haanibiriyani.hattrick.block.entity.ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        com.haanibiriyani.hattrick.block.menu.ModMenuTypes.MENUS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);
        modEventBus.addListener(this::onSpawnPlacementRegister);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            com.haanibiriyani.hattrick.network.ModNetwork.registerPackets();
        });
    }

    @SubscribeEvent
    public void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ENFORCER.get(), com.haanibiriyani.hattrick.entity.EnforcerEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.ENFORCER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> true,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        HatTrickCommand.register(event.getDispatcher());
    }
}