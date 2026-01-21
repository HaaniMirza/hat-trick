package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.block.EnforcementBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, HatTrickMod.MODID);

    public static final RegistryObject<Block> ENFORCEMENT_BLOCK = BLOCKS.register("enforcement_block",
            () -> new EnforcementBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F) // Unbreakable like bedrock
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    // Block items
    public static void registerBlockItems(DeferredRegister<Item> itemRegister) {
        itemRegister.register("enforcement_block",
                () -> new BlockItem(ENFORCEMENT_BLOCK.get(), new Item.Properties()));
    }
}