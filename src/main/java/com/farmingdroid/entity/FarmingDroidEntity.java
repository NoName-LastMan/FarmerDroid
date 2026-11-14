package com.farmingdroid.entity;

import com.farmingdroid.block.entity.DepotStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class FarmingDroidEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> FUEL = SynchedEntityData.defineId(FarmingDroidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> STATE = SynchedEntityData.defineId(FarmingDroidEntity.class, EntityDataSerializers.STRING);

    public static final int MAX_FUEL = 3200; // ~2.6 minutes of active operations
    private static final int SCAN_INTERVAL = 200; // 10 seconds (200 ticks)

    private ItemStack currentHoe = ItemStack.EMPTY;
    private final SimpleContainer internalInventory = new SimpleContainer(4);
    private BlockPos linkedDepotPos = null;

    private final List<BlockPos> taskQueue = new ArrayList<>();
    private int scanCooldown = SCAN_INTERVAL;
    private int workTimer = 0;

    public FarmingDroidEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FUEL, MAX_FUEL / 2);
        builder.define(STATE, DroidState.DOCKED_IDLE.name());
    }

    public int getFuel() {
        return this.entityData.get(FUEL);
    }

    public void setFuel(int fuel) {
        this.entityData.set(FUEL, Math.max(0, Math.min(MAX_FUEL, fuel)));
    }

    public void addFuel(int amount) {
        setFuel(getFuel() + amount);
    }

    public int getMaxFuel() {
        return MAX_FUEL;
    }

    public int getFuelPercentage() {
        return (int) (((float) getFuel() / MAX_FUEL) * 100);
    }

    public DroidState getDroidState() {
        try {
            return DroidState.valueOf(this.entityData.get(STATE));
        } catch (IllegalArgumentException e) {
            return DroidState.DOCKED_IDLE;
        }
    }

    public void setDroidState(DroidState state) {
        this.entityData.set(STATE, state.name());
    }

    public ItemStack getHoeItem() {
        return this.currentHoe;
    }

    public void setHoeItem(ItemStack hoe) {
        this.currentHoe = hoe != null ? hoe : ItemStack.EMPTY;
    }

    public BlockPos getLinkedDepotPos() {
        return linkedDepotPos;
    }

    public void setLinkedDepotPos(BlockPos pos) {
        this.linkedDepotPos = pos;
    }

    // Prevents trampling farmland when stepping on it
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            // Client-side visual thruster smoke particles when active
            if (getDroidState() == DroidState.WORKING || getDroidState() == DroidState.RETURNING) {
                if (this.random.nextFloat() < 0.6F) {
                    this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            this.getX() + (this.random.nextDouble() - 0.5D) * 0.2D,
                            this.getY() + 0.1D,
                            this.getZ() + (this.random.nextDouble() - 0.5D) * 0.2D,
                            0.0D, -0.05D, 0.0D);
                }
            }
            return;
        }

        // Server-side State Machine logic
        handleStateMachine();
    }

    private void handleStateMachine() {
        // Condition: Stalled if out of fuel
        if (getFuel() <= 0) {
            if (getDroidState() != DroidState.STALLED) {
                setDroidState(DroidState.STALLED);
                this.getNavigation().stop();
                this.level().playSound(null, this.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.7F, 0.8F);
            }
            return;
        }

        DroidState state = getDroidState();

        switch (state) {
            case STALLED:
                // If fuel was restored, recover
                if (getFuel() > 0) {
                    setDroidState(DroidState.RETURNING);
                }
                break;

            case DOCKED_IDLE:
                // Stay still at depot
                this.getNavigation().stop();

                scanCooldown--;
                if (scanCooldown <= 0) {
                    scanCooldown = SCAN_INTERVAL;
                    performPreflightAndScan();
                }
                break;

            case WORKING:
                handleWorkingState();
                break;

            case RETURNING:
                handleReturningState();
                break;
        }
    }

    private void performPreflightAndScan() {
        DepotStationBlockEntity depot = getDepotBlockEntity();
        if (depot == null) return;

        // Preflight: refuel from depot
        depot.refuelDroid(this);

        // Preflight: replace broken/empty hoe from depot
        if (this.currentHoe.isEmpty()) {
            depot.provideHoe(this);
        }

        // Must have both tool and fuel to start working
        if (this.currentHoe.isEmpty() || getFuel() <= 0) {
            return; // Stay frozen at depot
        }

        // Scan working area
        int radius = depot.getWorkingRadius();
        BlockPos center = depot.getBlockPos();
        this.taskQueue.clear();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos targetPos = center.offset(x, 0, z);
                BlockState state = level().getBlockState(targetPos);

                // Check mature crop
                if (state.getBlock() instanceof CropBlock crop) {
                    if (crop.isMaxAge(state)) {
                        taskQueue.add(targetPos);
                    }
                } else if (state.getBlock() instanceof FarmBlock) {
                    // Check empty farmland above
                    BlockPos abovePos = targetPos.above();
                    if (level().getBlockState(abovePos).isAir()) {
                        taskQueue.add(abovePos);
                    }
                }
            }
        }

        if (!taskQueue.isEmpty()) {
            setDroidState(DroidState.WORKING);
            this.level().playSound(null, this.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.NEUTRAL, 0.6F, 1.4F);
        }
    }

    private void handleWorkingState() {
        // Consume fuel during work
        setFuel(getFuel() - 1);

        // Check if hoe broke
        if (this.currentHoe.isEmpty()) {
            setDroidState(DroidState.RETURNING);
            return;
        }

        // Check if internal inventory is full
        if (isInventoryFull()) {
            setDroidState(DroidState.RETURNING);
            return;
        }

        if (taskQueue.isEmpty()) {
            setDroidState(DroidState.RETURNING);
            return;
        }

        BlockPos targetPos = taskQueue.get(0);
        double distSq = this.distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);

        if (distSq > 2.5D) {
            this.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.0D);
        } else {
            // Arrived at target crop
            this.getNavigation().stop();
            workTimer++;

            if (workTimer >= 15) { // ~0.75 second to harvest & replant
                workTimer = 0;
                executeCropWork(targetPos);
                taskQueue.remove(0);
            }
        }
    }

    private void executeCropWork(BlockPos pos) {
        BlockState state = level().getBlockState(pos);

        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            // Harvest: drop items and gather into internal inventory
            List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level(), pos, null, this, this.currentHoe);
            level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

            for (ItemStack drop : drops) {
                ItemStack remaining = insertIntoInternalInventory(drop);
                if (!remaining.isEmpty()) {
                    Block.popResource(level(), pos, remaining);
                }
            }

            // Damage hoe
            if (this.currentHoe.isDamageableItem()) {
                this.currentHoe.setDamageValue(this.currentHoe.getDamageValue() + 1);
                if (this.currentHoe.getDamageValue() >= this.currentHoe.getMaxDamage()) {
                    this.currentHoe = ItemStack.EMPTY;
                    level().playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 0.8F, 1.0F);
                }
            }

            // Replant: check if we have matching seed in inventory (STRICT: no free generation)
            Item seedItem = getSeedForCrop(crop);
            if (seedItem != null && consumeSeed(seedItem)) {
                level().setBlock(pos, crop.getStateForAge(0), 3);
                level().playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.8F, 1.1F);
            }
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
        for (int i = 0; i < internalInventory.getContainerSize(); i++) {
            ItemStack stack = internalInventory.getItem(i);
            if (!stack.isEmpty() && stack.is(seedItem)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private ItemStack insertIntoInternalInventory(ItemStack stack) {
        for (int i = 0; i < internalInventory.getContainerSize(); i++) {
            ItemStack slotStack = internalInventory.getItem(i);
            if (slotStack.isEmpty()) {
                internalInventory.setItem(i, stack.copy());
                return ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(slotStack, stack)) {
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                int toAdd = Math.min(space, stack.getCount());
                if (toAdd > 0) {
                    slotStack.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < internalInventory.getContainerSize(); i++) {
            if (internalInventory.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    private void handleReturningState() {
        DepotStationBlockEntity depot = getDepotBlockEntity();
        if (depot == null) {
            setDroidState(DroidState.DOCKED_IDLE);
            return;
        }

        BlockPos depotPos = depot.getBlockPos();
        double distSq = this.distanceToSqr(depotPos.getX() + 0.5D, depotPos.getY() + 1.0D, depotPos.getZ() + 0.5D);

        if (distSq > 2.0D) {
            this.getNavigation().moveTo(depotPos.getX() + 0.5D, depotPos.getY() + 1.0D, depotPos.getZ() + 0.5D, 1.0D);
        } else {
            // Arrived at depot: deposit all harvests
            this.getNavigation().stop();
            for (int i = 0; i < internalInventory.getContainerSize(); i++) {
                ItemStack stack = internalInventory.getItem(i);
                if (!stack.isEmpty()) {
                    ItemStack remaining = depot.depositItem(stack);
                    internalInventory.setItem(i, remaining);
                }
            }

            // Snap accurately onto depot block
            this.setPos(depotPos.getX() + 0.5D, depotPos.getY() + 0.9D, depotPos.getZ() + 0.5D);
            setDroidState(DroidState.DOCKED_IDLE);
            this.level().playSound(null, depotPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, 1.4F);
        }
    }

    public DepotStationBlockEntity getDepotBlockEntity() {
        if (linkedDepotPos != null && level().isLoaded(linkedDepotPos)) {
            BlockEntity be = level().getBlockEntity(linkedDepotPos);
            if (be instanceof DepotStationBlockEntity depot) {
                return depot;
            }
        }
        // Auto-find adjacent or supporting depot if not linked
        BlockPos under = this.blockPosition().below();
        if (level().getBlockEntity(under) instanceof DepotStationBlockEntity depot) {
            this.linkedDepotPos = under;
            return depot;
        }
        return null;
    }

    // Direct manual interaction: right-click with fuel when stalled or running low
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (!level().isClientSide()) {
            DepotStationBlockEntity depot = getDepotBlockEntity();
            boolean isFuel = (depot != null && depot.isFuel(itemInHand)) || itemInHand.is(Items.COAL) || itemInHand.is(Items.CHARCOAL);

            if (isFuel && getFuel() < MAX_FUEL) {
                addFuel(1600);
                if (!player.getAbilities().instabuild) {
                    itemInHand.shrink(1);
                }

                // If stalled, wake up and return to depot
                if (getDroidState() == DroidState.STALLED) {
                    setDroidState(DroidState.RETURNING);
                }

                level().playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.7F, 1.2F);
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    public void writeToItemTag(ItemStack itemStack) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Fuel", getFuel());
        if (!currentHoe.isEmpty()) {
            tag.put("HoeItem", currentHoe.save(level().registryAccess()));
        }
        if (linkedDepotPos != null) {
            tag.putLong("LinkedDepot", linkedDepotPos.asLong());
        }
        // Save NBT onto ItemStack
        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
    }

    public void readFromItemTag(ItemStack itemStack) {
        net.minecraft.world.item.component.CustomData customData =
                itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("Fuel")) {
                setFuel(tag.getInt("Fuel"));
            }
            if (tag.contains("HoeItem")) {
                ItemStack.parse(level().registryAccess(), tag.getCompound("HoeItem"))
                        .ifPresent(this::setHoeItem);
            }
            if (tag.contains("LinkedDepot")) {
                this.linkedDepotPos = BlockPos.of(tag.getLong("LinkedDepot"));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuel", getFuel());
        tag.putString("DroidState", getDroidState().name());
        if (!currentHoe.isEmpty()) {
            tag.put("HoeItem", currentHoe.save(level().registryAccess()));
        }
        if (linkedDepotPos != null) {
            tag.putLong("LinkedDepot", linkedDepotPos.asLong());
        }
        net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < internalInventory.getContainerSize(); i++) {
            ItemStack stack = internalInventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                itemTag.put("Item", stack.save(level().registryAccess()));
                listTag.add(itemTag);
            }
        }
        tag.put("InternalInv", listTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Fuel")) setFuel(tag.getInt("Fuel"));
        if (tag.contains("DroidState")) {
            try {
                setDroidState(DroidState.valueOf(tag.getString("DroidState")));
            } catch (Exception ignored) {}
        }
        if (tag.contains("HoeItem")) {
            ItemStack.parse(level().registryAccess(), tag.getCompound("HoeItem"))
                    .ifPresent(this::setHoeItem);
        }
        if (tag.contains("LinkedDepot")) {
            this.linkedDepotPos = BlockPos.of(tag.getLong("LinkedDepot"));
        }
        if (tag.contains("InternalInv")) {
            internalInventory.clearContent();
            net.minecraft.nbt.ListTag listTag = tag.getList("InternalInv", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot < internalInventory.getContainerSize() && itemTag.contains("Item")) {
                    ItemStack.parse(level().registryAccess(), itemTag.getCompound("Item"))
                            .ifPresent(stack -> internalInventory.setItem(slot, stack));
                }
            }
        }
    }
}
