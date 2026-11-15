package com.farmingdroid.entity.ai;

import com.droids.core.entity.ai.AbstractDroidRoutineGoal;
import com.farmingdroid.entity.FarmingDroidEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class FarmingDroidHarvestGoal extends AbstractDroidRoutineGoal<FarmingDroidEntity> {

    public FarmingDroidHarvestGoal(FarmingDroidEntity droid, double speedModifier) {
        super(droid, speedModifier);
    }

    @Override
    public List<BlockPos> findWorkTargets(Level level, BlockPos depotPos, int radius) {
        List<BlockPos> targets = new ArrayList<>();
        int currentY = depotPos.getY();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = currentY - 2; y <= currentY + 2; y++) {
                    BlockPos farmPos = depotPos.offset(x, y - currentY, z);
                    BlockState farmState = level.getBlockState(farmPos);

                    if (farmState.is(Blocks.FARMLAND)) {
                        BlockPos cropPos = farmPos.above();
                        BlockState cropState = level.getBlockState(cropPos);

                        if (cropState.getBlock() instanceof CropBlock crop && crop.isMaxAge(cropState)) {
                            targets.add(cropPos);
                            break;
                        }
                    }
                }
            }
        }
        return targets;
    }

    @Override
    public boolean performWork(BlockPos cropPos, ServerLevel serverLevel) {
        BlockState cropState = serverLevel.getBlockState(cropPos);

        if (cropState.getBlock() instanceof CropBlock crop && crop.isMaxAge(cropState)) {
            ItemStack hoe = this.droid.getToolItem();
            List<ItemStack> drops = Block.getDrops(cropState, serverLevel, cropPos, null, this.droid, hoe);

            Item seedItem = getSeedForCrop(crop);
            boolean replanted = false;

            for (ItemStack drop : drops) {
                ItemStack remaining = this.droid.getInventory().addItem(drop);
                if (!remaining.isEmpty()) {
                    Block.popResource(serverLevel, cropPos, remaining);
                }
            }

            // Consume 1 seed to replant
            if (seedItem != null && consumeSeed(seedItem)) {
                serverLevel.setBlock(cropPos, crop.getStateForAge(0), 3);
                replanted = true;
            } else {
                serverLevel.setBlock(cropPos, Blocks.AIR.defaultBlockState(), 3);
            }

            serverLevel.playSound(null, cropPos,
                    replanted ? SoundEvents.CROP_PLANTED : SoundEvents.CROP_BREAK,
                    SoundSource.BLOCKS, 1.0F, 1.1F);

            this.droid.damageTool(1);
            return true;
        }
        return false;
    }

    private Item getSeedForCrop(CropBlock crop) {
        if (crop == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (crop == Blocks.CARROTS) return Items.CARROT;
        if (crop == Blocks.POTATOES) return Items.POTATO;
        if (crop == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        return null;
    }

    private boolean consumeSeed(Item seedItem) {
        Container inv = this.droid.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(seedItem)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getDepositAmount(ItemStack stack) {
        if (isProduce(stack.getItem())) {
            return stack.getCount();
        } else if (isSeed(stack.getItem()) && stack.getCount() > 8) {
            // Keep 8 seeds in internal buffer for replanting
            return stack.getCount() - 8;
        }
        return 0;
    }

    private boolean isProduce(Item item) {
        return item == Items.WHEAT || item == Items.CARROT || item == Items.POTATO
                || item == Items.BEETROOT;
    }

    private boolean isSeed(Item item) {
        return item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS;
    }
}
