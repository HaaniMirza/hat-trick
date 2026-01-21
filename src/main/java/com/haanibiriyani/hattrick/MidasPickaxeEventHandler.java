package com.haanibiriyani.hattrick;

import com.haanibiriyani.hattrick.item.MidasPickaxeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = HatTrickMod.MODID)
public class MidasPickaxeEventHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        ItemStack heldItem = event.getPlayer().getMainHandItem();

        // Check if using Midas Pickaxe
        boolean isMidasPickaxe = heldItem.getItem() instanceof MidasPickaxeItem;

        // Check if tool has Curse of Midas
        boolean hasCurseOfMidas = heldItem.getEnchantmentLevel(ModEnchantments.CURSE_OF_MIDAS.get()) > 0;

        if (!isMidasPickaxe && !hasCurseOfMidas) {
            return;
        }

        BlockState state = event.getState();
        Block block = state.getBlock();
        BlockPos pos = event.getPos();

        // Don't convert air, bedrock, or other unbreakable blocks
        if (block == Blocks.AIR || block == Blocks.BEDROCK ||
                block == Blocks.BARRIER || block == Blocks.COMMAND_BLOCK) {
            return;
        }

        Level level = (Level) event.getLevel();

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {

            if (isMidasPickaxe) {
                // Midas Pickaxe: Always drop only gold (original behavior)
                handleMidasPickaxe(serverLevel, pos, block, state, event);
            } else if (hasCurseOfMidas) {
                // Curse of Midas: 50% chance for normal drops + gold, 50% chance for only gold
                handleCurseOfMidas(serverLevel, pos, block, state, event, heldItem);
            }
        }
    }

    private static void handleMidasPickaxe(ServerLevel level, BlockPos pos, Block block, BlockState state, BlockEvent.BreakEvent event) {
        // Remove the block without dropping items
        level.destroyBlock(pos, false);

        // Determine what gold to drop
        ItemStack goldDrop = determineGoldDrop(block);

        if (!goldDrop.isEmpty()) {
            // Spawn the gold at the block position
            spawnItemAtPosition(level, pos, goldDrop);
        }

        // Cancel the event so the block doesn't break twice
        event.setCanceled(true);
    }

    private static void handleCurseOfMidas(ServerLevel level, BlockPos pos, Block block, BlockState state,
                                           BlockEvent.BreakEvent event, ItemStack tool) {
        // 50% chance: drop normal items + gold
        // 50% chance: drop only gold
        boolean dropNormalItems = RANDOM.nextBoolean();

        if (dropNormalItems) {
            // Let the block break normally and drop its items
            // Then add gold as a bonus
            ItemStack goldDrop = determineGoldDrop(block);
            if (!goldDrop.isEmpty()) {
                spawnItemAtPosition(level, pos, goldDrop);
            }
            // Don't cancel the event - let normal drops happen
        } else {
            // Drop only gold (no normal items)
            level.destroyBlock(pos, false);

            ItemStack goldDrop = determineGoldDrop(block);
            if (!goldDrop.isEmpty()) {
                spawnItemAtPosition(level, pos, goldDrop);
            }

            event.setCanceled(true);
        }
    }

    private static void spawnItemAtPosition(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                stack
        );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    private static ItemStack determineGoldDrop(Block block) {
        // Check if it's a full block (using properties as a heuristic)
        if (isFullBlock(block)) {
            return new ItemStack(Items.GOLD_BLOCK, 1);
        } else {
            // Non-full blocks drop 5-8 gold ingots or raw gold
            int amount = 5 + RANDOM.nextInt(4); // 5-8

            // 50% chance for raw gold, 50% for ingots
            if (RANDOM.nextBoolean()) {
                return new ItemStack(Items.RAW_GOLD, amount);
            } else {
                return new ItemStack(Items.GOLD_INGOT, amount);
            }
        }
    }

    private static boolean isFullBlock(Block block) {
        // Check common full blocks
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();

        // Most stone variants, dirt, etc. are full blocks
        // Removed: ores, sand, gravel (these will drop ingots/raw gold instead)
        if (blockId.contains("stone") ||
                blockId.contains("dirt") ||
                blockId.contains("grass_block") ||
                blockId.contains("netherrack") ||
                blockId.contains("end_stone") ||
                blockId.contains("obsidian") ||
                blockId.contains("concrete") ||
                blockId.contains("terracotta") ||
                blockId.contains("planks") ||
                blockId.contains("log") ||
                blockId.contains("wood") ||
                blockId.contains("_block")) {
            return true;
        }

        // Check if it's in the common full block list
        if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK ||
                block == Blocks.STONE || block == Blocks.COBBLESTONE ||
                block == Blocks.CLAY || block == Blocks.ICE ||
                block == Blocks.NETHERRACK || block == Blocks.END_STONE ||
                block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
            return true;
        }

        // Default to non-full block (which gives ingots/raw gold)
        return false;
    }
}