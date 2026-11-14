package com.farmingdroid.block.entity;

import com.farmingdroid.entity.FarmingDroidEntity;
import com.farmingdroid.init.ModBlockEntities;
import com.farmingdroid.item.RangeUpgradeItem;
import com.farmingdroid.menu.DepotStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DepotStationBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int TOTAL_SLOTS = 12;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_HOE = 1;
    public static final int SLOT_UPGRADE = 2;
    public static final int OUTPUT_SLOT_START = 3;
    public static final int OUTPUT_SLOT_COUNT = 9;

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    public DepotStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DEPOT_STATION.get(), pos, blockState);
    }

    public int getWorkingRadius() {
        ItemStack upgradeStack = items.get(SLOT_UPGRADE);
        if (!upgradeStack.isEmpty() && upgradeStack.getItem() instanceof RangeUpgradeItem upgradeItem) {
            return upgradeItem.getTier().getRadius();
        }
        return 1; // Default radius: 1 (3x3 area)
    }

    public boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        try {
            int burn = stack.getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING);
            if (burn > 0) return true;
        } catch (Exception ignored) {}
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(Items.BLAZE_ROD) || stack.is(Items.COAL_BLOCK);
    }

    public int getBurnDuration(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        try {
            int burn = stack.getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING);
            if (burn > 0) return burn;
        } catch (Exception ignored) {}
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 1600;
        if (stack.is(Items.BLAZE_ROD)) return 2400;
        if (stack.is(Items.COAL_BLOCK)) return 16000;
        return 300;
    }

    public void refuelDroid(FarmingDroidEntity droid) {
        if (droid.getFuel() >= droid.getMaxFuel()) return;

        ItemStack fuelStack = items.get(SLOT_FUEL);
        if (!fuelStack.isEmpty() && isFuel(fuelStack)) {
            int duration = getBurnDuration(fuelStack);
            droid.addFuel(duration);
            fuelStack.shrink(1);
            setChanged();
        }
    }

    public boolean isHoe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(net.minecraft.tags.ItemTags.HOES) || stack.getItem() instanceof HoeItem;
    }

    public void provideHoe(FarmingDroidEntity droid) {
        if (!droid.getHoeItem().isEmpty()) return;

        ItemStack hoeStack = items.get(SLOT_HOE);
        if (isHoe(hoeStack)) {
            droid.setHoeItem(hoeStack.copy());
            items.set(SLOT_HOE, ItemStack.EMPTY);
            setChanged();
        }
    }

    public ItemStack depositItem(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        // Try merge into existing stacks first
        for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT; i++) {
            ItemStack current = items.get(i);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack)) {
                int space = current.getMaxStackSize() - current.getCount();
                int toAdd = Math.min(space, stack.getCount());
                if (toAdd > 0) {
                    current.grow(toAdd);
                    stack.shrink(toAdd);
                    setChanged();
                    if (stack.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }

        // Try insert into empty slot
        for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.copy());
                setChanged();
                return ItemStack.EMPTY;
            }
        }

        return stack; // Remaining that could not fit
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.farmingdroid.depot_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DepotStationMenu(containerId, playerInventory, this);
    }
}
