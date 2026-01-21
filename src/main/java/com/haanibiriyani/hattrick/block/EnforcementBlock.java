package com.haanibiriyani.hattrick.block;

import com.haanibiriyani.hattrick.block.entity.EnforcementBlockEntity;
import com.haanibiriyani.hattrick.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class EnforcementBlock extends BaseEntityBlock {

    public EnforcementBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnforcementBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Only allow creative mode players to open
        if (!player.isCreative()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof EnforcementBlockEntity enforcementBlock) {
                // Sync data to client before opening GUI
                com.haanibiriyani.hattrick.network.ModNetwork.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new com.haanibiriyani.hattrick.network.SyncEnforcementBlockPacket(
                                pos,
                                enforcementBlock.getRadius(),
                                enforcementBlock.getCommand()
                        )
                );

                // Small delay to ensure packet is processed before opening GUI
                ((ServerPlayer) player).getServer().execute(() -> {
                    NetworkHooks.openScreen((ServerPlayer) player, enforcementBlock, pos);
                });
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof EnforcementBlockEntity enforcementBlock) {
                enforcementBlock.clearAllEffects();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        // Only allow creative mode players to break
        return player.isCreative() ? super.getDestroyProgress(state, player, level, pos) : 0.0F;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ENFORCEMENT_BLOCK.get(), EnforcementBlockEntity::tick);
    }
}