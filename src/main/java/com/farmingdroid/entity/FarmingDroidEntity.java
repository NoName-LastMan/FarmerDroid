package com.farmingdroid.entity;

import com.farmingdroid.block.entity.DepotStationBlockEntity;
import com.farmingdroid.entity.ai.FarmingDroidHarvestGoal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FarmingDroidEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> FUEL = SynchedEntityData.defineId(FarmingDroidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> STATE = SynchedEntityData.defineId(FarmingDroidEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<BlockPos>> LINKED_DEPOT = SynchedEntityData.defineId(FarmingDroidEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public static final int MAX_FUEL = 3200; // ~2.6 minutes of continuous operation

    private ItemStack currentHoe = ItemStack.EMPTY;
    private final SimpleContainer inventory = new SimpleContainer(9);

    public FarmingDroidEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FarmingDroidHarvestGoal(this, 1.0D));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FUEL, MAX_FUEL / 2);
        builder.define(STATE, DroidState.DOCKED_IDLE.name());
        builder.define(LINKED_DEPOT, Optional.empty());
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

    public boolean hasFuel() {
        return getFuel() > 0;
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

    public boolean hasHoe() {
        return !this.currentHoe.isEmpty();
    }

    public void damageHoe(int amount) {
        if (!this.currentHoe.isEmpty() && this.currentHoe.isDamageableItem()) {
            this.currentHoe.setDamageValue(this.currentHoe.getDamageValue() + amount);
            if (this.currentHoe.getDamageValue() >= this.currentHoe.getMaxDamage()) {
                this.currentHoe = ItemStack.EMPTY;
                this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 0.8F, 1.0F);
            }
        }
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public boolean isInventoryFull() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (this.inventory.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    @Nullable
    public BlockPos getLinkedDepotPos() {
        return this.entityData.get(LINKED_DEPOT).orElseGet(() -> {
            BlockPos under = this.blockPosition().below();
            if (level().getBlockEntity(under) instanceof DepotStationBlockEntity) {
                setLinkedDepotPos(under);
                return under;
            }
            return null;
        });
    }

    public void setLinkedDepotPos(@Nullable BlockPos pos) {
        this.entityData.set(LINKED_DEPOT, Optional.ofNullable(pos));
    }

    public DepotStationBlockEntity getDepotBlockEntity() {
        BlockPos pos = getLinkedDepotPos();
        if (pos != null && level().isLoaded(pos)) {
            BlockEntity be = level().getBlockEntity(pos);
            if (be instanceof DepotStationBlockEntity depot) {
                return depot;
            }
        }
        return null;
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
            // Client-side visual thruster smoke particles when working or returning
            DroidState state = getDroidState();
            if ((state == DroidState.WORKING || state == DroidState.RETURNING) && hasFuel()) {
                if (this.random.nextFloat() < 0.6F) {
                    this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            this.getX() + (this.random.nextDouble() - 0.5D) * 0.2D,
                            this.getY() + 0.1D,
                            this.getZ() + (this.random.nextDouble() - 0.5D) * 0.2D,
                            0.0D, -0.05D, 0.0D);
                }
            }
        } else {
            // Server-side fuel consumption during active work
            if (getDroidState() == DroidState.WORKING) {
                if (getFuel() > 0) {
                    setFuel(getFuel() - 1);
                }
            }

            if (getFuel() <= 0 && getDroidState() != DroidState.STALLED) {
                setDroidState(DroidState.STALLED);
                this.getNavigation().stop();
                this.level().playSound(null, this.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.7F, 0.8F);
            }
        }
    }

    // Manual interaction: right-click with hoe or fuel
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        // 1. Hoe Interaction
        boolean isHoe = itemInHand.is(ItemTags.HOES) || itemInHand.getItem() instanceof HoeItem;
        if (isHoe) {
            if (!level().isClientSide()) {
                if (this.currentHoe.isEmpty()) {
                    setHoeItem(itemInHand.copyWithCount(1));
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                    }
                    level().playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.NEUTRAL, 0.8F, 1.2F);
                    player.displayClientMessage(
                            Component.translatable("message.farmingdroid.hoe_equipped", this.currentHoe.getHoverName()).withStyle(ChatFormatting.GREEN),
                            true
                    );
                    return InteractionResult.SUCCESS;
                } else if (player.isShiftKeyDown()) {
                    ItemStack oldHoe = this.currentHoe.copy();
                    setHoeItem(itemInHand.copyWithCount(1));
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1);
                        if (!player.getInventory().add(oldHoe)) {
                            this.spawnAtLocation(oldHoe);
                        }
                    }
                    level().playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.NEUTRAL, 0.8F, 1.2F);
                    player.displayClientMessage(
                            Component.translatable("message.farmingdroid.hoe_swapped", this.currentHoe.getHoverName()).withStyle(ChatFormatting.GREEN),
                            true
                    );
                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.farmingdroid.hoe_already_equipped", this.currentHoe.getHoverName()).withStyle(ChatFormatting.YELLOW),
                            true
                    );
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        // 2. Fuel Interaction
        if (!level().isClientSide()) {
            DepotStationBlockEntity depot = getDepotBlockEntity();
            boolean isFuel = (depot != null && depot.isFuel(itemInHand)) || itemInHand.is(Items.COAL) || itemInHand.is(Items.CHARCOAL);

            if (isFuel && getFuel() < MAX_FUEL) {
                addFuel(1600);
                if (!player.getAbilities().instabuild) {
                    itemInHand.shrink(1);
                }

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
        BlockPos depotPos = getLinkedDepotPos();
        if (depotPos != null) {
            tag.putLong("LinkedDepot", depotPos.asLong());
        }
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
                setLinkedDepotPos(BlockPos.of(tag.getLong("LinkedDepot")));
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
        BlockPos depotPos = getLinkedDepotPos();
        if (depotPos != null) {
            tag.putLong("LinkedDepot", depotPos.asLong());
        }

        ListTag listTag = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                itemTag.put("Item", stack.save(level().registryAccess()));
                listTag.add(itemTag);
            }
        }
        tag.put("Inventory", listTag);
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
            setLinkedDepotPos(BlockPos.of(tag.getLong("LinkedDepot")));
        }
        if (tag.contains("Inventory")) {
            inventory.clearContent();
            ListTag listTag = tag.getList("Inventory", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot < inventory.getContainerSize() && itemTag.contains("Item")) {
                    ItemStack.parse(level().registryAccess(), itemTag.getCompound("Item"))
                            .ifPresent(stack -> inventory.setItem(slot, stack));
                }
            }
        }
    }
}
