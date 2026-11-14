package com.farmingdroid.entity.ai;

import com.farmingdroid.block.entity.DepotStationBlockEntity;
import com.farmingdroid.entity.DroidState;
import com.farmingdroid.entity.FarmingDroidEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * High-performance Automated Guided Batch Routine for Farming Droid.
 * 
 * 1. IDLE: Rests motionless at Depot Station dock. Fast-checks fuel/hoe, scans field on 10s timer.
 * 2. HARVESTING: Compiles all mature crops into an optimized Nearest-Neighbor route, chain-harvesting with hoe.
 * 3. RETURNING: Returns to Depot Station dock, unloads harvests, refuels, and enters standby.
 */
public class FarmingDroidHarvestGoal extends Goal {

    public enum RoutineState {
        IDLE,
        HARVESTING,
        RETURNING
    }

    private final FarmingDroidEntity droid;
    private final double speedModifier;
    private RoutineState state = RoutineState.IDLE;

    private final Deque<BlockPos> harvestQueue = new ArrayDeque<>();
    private BlockPos currentCropTarget = null;

    private int scanTimer = 0;
    private int actionTicks = 0;
    private int repathTimer = 0;

    public FarmingDroidHarvestGoal(FarmingDroidEntity droid, double speedModifier) {
        this.droid = droid;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.droid.hasFuel() && this.droid.getLinkedDepotPos() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.droid.hasFuel() && this.droid.getLinkedDepotPos() != null;
    }

    @Override
    public void tick() {
        // Out of fuel condition: stall on the spot
        if (!this.droid.hasFuel()) {
            this.droid.getNavigation().stop();
            this.state = RoutineState.IDLE;
            this.droid.setDroidState(DroidState.STALLED);
            return;
        }

        switch (this.state) {
            case IDLE -> tickIdle();
            case HARVESTING -> tickHarvesting();
            case RETURNING -> tickReturning();
        }
    }

    // 1. IDLE: Wait at Depot Station dock and scan area periodically
    private void tickIdle() {
        this.droid.setDroidState(DroidState.DOCKED_IDLE);
        this.droid.getNavigation().stop();

        // Frequently check depot for missing hoe or fuel (every 10 ticks)
        if (this.droid.tickCount % 10 == 0) {
            DepotStationBlockEntity depot = this.droid.getDepotBlockEntity();
            if (depot != null) {
                if (!this.droid.hasHoe()) depot.provideHoe(this.droid);
                if (this.droid.getFuel() < this.droid.getMaxFuel()) depot.refuelDroid(this.droid);
            }
        }

        this.scanTimer++;

        // Periodic field scan every 10 seconds (200 ticks)
        if (this.scanTimer >= 200) {
            this.scanTimer = 0;

            // Must have both hoe and fuel to start a routine
            if (!this.droid.hasHoe() || !this.droid.hasFuel()) {
                return;
            }

            planHarvestRoute();

            if (!this.harvestQueue.isEmpty()) {
                this.currentCropTarget = this.harvestQueue.poll();
                this.state = RoutineState.HARVESTING;
                this.droid.setDroidState(DroidState.WORKING);
                this.droid.getNavigation().moveTo(
                        this.currentCropTarget.getX() + 0.5D,
                        this.currentCropTarget.getY(),
                        this.currentCropTarget.getZ() + 0.5D,
                        this.speedModifier
                );
            } else if (hasHarvestsToDeposit()) {
                this.state = RoutineState.RETURNING;
            }
        }
    }

    // Compiles all mature crops in the bounding box and sorts them via Nearest-Neighbor heuristic
    private void planHarvestRoute() {
        this.harvestQueue.clear();
        Level level = this.droid.level();

        DepotStationBlockEntity depot = this.droid.getDepotBlockEntity();
        if (depot == null) return;

        int radius = depot.getWorkingRadius();
        BlockPos center = depot.getBlockPos();
        int currentY = center.getY();

        List<BlockPos> found = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Check farmland and crop above with vertical tolerance +-2
                for (int y = currentY - 2; y <= currentY + 2; y++) {
                    BlockPos farmPos = center.offset(x, y - currentY, z);
                    BlockState farmState = level.getBlockState(farmPos);

                    if (farmState.is(Blocks.FARMLAND)) {
                        BlockPos cropPos = farmPos.above();
                        BlockState cropState = level.getBlockState(cropPos);

                        if (cropState.getBlock() instanceof CropBlock crop && crop.isMaxAge(cropState)) {
                            found.add(cropPos);
                            break;
                        }
                    }
                }
            }
        }

        if (found.isEmpty()) return;

        // Sort found targets with Nearest-Neighbor path heuristic
        BlockPos currentPos = this.droid.blockPosition();
        while (!found.isEmpty()) {
            BlockPos nearest = null;
            double nearestDistSq = Double.MAX_VALUE;

            for (BlockPos p : found) {
                double dSq = currentPos.distSqr(p);
                if (dSq < nearestDistSq) {
                    nearestDistSq = dSq;
                    nearest = p;
                }
            }

            if (nearest != null) {
                this.harvestQueue.add(nearest);
                found.remove(nearest);
                currentPos = nearest;
            }
        }
    }

    // 2. HARVESTING: Chain-harvests crops without pauses
    private void tickHarvesting() {
        // Hoe broken condition: abort and return
        if (!this.droid.hasHoe()) {
            this.currentCropTarget = null;
            this.harvestQueue.clear();
            this.state = RoutineState.RETURNING;
            return;
        }

        // Inventory full condition: abort and return
        if (this.droid.isInventoryFull()) {
            this.currentCropTarget = null;
            this.harvestQueue.clear();
            this.state = RoutineState.RETURNING;
            return;
        }

        if (this.currentCropTarget == null) {
            if (!this.harvestQueue.isEmpty()) {
                this.currentCropTarget = this.harvestQueue.poll();
            } else {
                this.state = RoutineState.RETURNING;
                return;
            }
        }

        double distSq = this.droid.distanceToSqr(
                this.currentCropTarget.getX() + 0.5D,
                this.currentCropTarget.getY(),
                this.currentCropTarget.getZ() + 0.5D
        );

        this.droid.getLookControl().setLookAt(
                this.currentCropTarget.getX() + 0.5D,
                this.currentCropTarget.getY() + 0.5D,
                this.currentCropTarget.getZ() + 0.5D
        );

        if (distSq <= 4.0D) {
            this.actionTicks++;
            this.droid.setDroidState(DroidState.WORKING);

            // Harvest action delay: 6 ticks (~0.3s)
            if (this.actionTicks >= 6) {
                harvestAt(this.currentCropTarget);
                this.actionTicks = 0;

                // Instantly advance to next crop in queue
                if (!this.harvestQueue.isEmpty()) {
                    this.currentCropTarget = this.harvestQueue.poll();
                    this.droid.getNavigation().moveTo(
                            this.currentCropTarget.getX() + 0.5D,
                            this.currentCropTarget.getY(),
                            this.currentCropTarget.getZ() + 0.5D,
                            this.speedModifier
                    );
                } else {
                    this.currentCropTarget = null;
                    this.state = RoutineState.RETURNING;
                }
            }
        } else {
            if (++this.repathTimer >= 20 || this.droid.getNavigation().isDone()) {
                this.repathTimer = 0;
                this.droid.getNavigation().moveTo(
                        this.currentCropTarget.getX() + 0.5D,
                        this.currentCropTarget.getY(),
                        this.currentCropTarget.getZ() + 0.5D,
                        this.speedModifier
                );
            }
        }
    }

    private void harvestAt(BlockPos cropPos) {
        if (!(this.droid.level() instanceof ServerLevel serverLevel)) return;

        BlockState cropState = serverLevel.getBlockState(cropPos);

        if (cropState.getBlock() instanceof CropBlock crop && crop.isMaxAge(cropState)) {
            ItemStack hoe = this.droid.getHoeItem();
            List<ItemStack> drops = Block.getDrops(cropState, serverLevel, cropPos, null, this.droid, hoe);

            // Replant: check seed from droid internal inventory (STRICT: no free duplication)
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

            // Damage hoe
            this.droid.damageHoe(1);
        }
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

    // 3. RETURNING: Return to Depot Station dock, deposit produce, and enter standby
    private void tickReturning() {
        this.droid.setDroidState(DroidState.RETURNING);
        BlockPos depotPos = this.droid.getLinkedDepotPos();
        if (depotPos == null) {
            this.state = RoutineState.IDLE;
            return;
        }

        double distSq = this.droid.distanceToSqr(depotPos.getX() + 0.5D, depotPos.getY() + 1.0D, depotPos.getZ() + 0.5D);
        this.droid.getLookControl().setLookAt(depotPos.getX() + 0.5D, depotPos.getY() + 1.0D, depotPos.getZ() + 0.5D);

        if (distSq <= 3.0D) {
            // Arrived at depot: deposit harvests
            DepotStationBlockEntity depot = this.droid.getDepotBlockEntity();
            if (depot != null) {
                depositHarvestsToDepot(depot);
                depot.refuelDroid(this.droid);
                if (!this.droid.hasHoe()) depot.provideHoe(this.droid);
            }

            // Snap accurately onto depot dock pad
            this.droid.setPos(depotPos.getX() + 0.5D, depotPos.getY() + 0.9D, depotPos.getZ() + 0.5D);
            this.droid.getNavigation().stop();
            this.state = RoutineState.IDLE;
            this.scanTimer = 0;
            this.droid.setDroidState(DroidState.DOCKED_IDLE);
            this.droid.level().playSound(null, depotPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.6F, 1.4F);
        } else {
            if (++this.repathTimer >= 20 || this.droid.getNavigation().isDone()) {
                this.repathTimer = 0;
                this.droid.getNavigation().moveTo(
                        depotPos.getX() + 0.5D,
                        depotPos.getY() + 1.0D,
                        depotPos.getZ() + 0.5D,
                        this.speedModifier * 0.9D
                );
            }
        }
    }

    private void depositHarvestsToDepot(DepotStationBlockEntity depot) {
        Container inv = this.droid.getInventory();
        boolean transferredAny = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            int transferableAmount = 0;
            if (isProduce(stack.getItem())) {
                transferableAmount = stack.getCount();
            } else if (isSeed(stack.getItem()) && stack.getCount() > 8) {
                // Keep 8 seeds in internal buffer for replanting, deposit the rest
                transferableAmount = stack.getCount() - 8;
            }

            if (transferableAmount > 0) {
                ItemStack toTransfer = stack.split(transferableAmount);
                ItemStack remaining = depot.depositItem(toTransfer);

                if (!remaining.isEmpty()) {
                    stack.grow(remaining.getCount());
                } else {
                    transferredAny = true;
                }
            }
        }

        if (transferredAny) {
            this.droid.level().playSound(null, depot.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
    }

    private boolean hasHarvestsToDeposit() {
        Container inv = this.droid.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (isProduce(stack.getItem())) return true;
                if (isSeed(stack.getItem()) && stack.getCount() > 8) return true;
            }
        }
        return false;
    }

    private boolean isProduce(Item item) {
        return item == Items.WHEAT || item == Items.CARROT || item == Items.POTATO
                || item == Items.BEETROOT;
    }

    private boolean isSeed(Item item) {
        return item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS;
    }
}
