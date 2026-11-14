package com.farmingdroid.menu;

import com.farmingdroid.block.entity.DepotStationBlockEntity;
import com.farmingdroid.init.ModBlocks;
import com.farmingdroid.init.ModMenuTypes;
import com.farmingdroid.item.RangeUpgradeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

public class DepotStationMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerLevelAccess access;

    // Client-side constructor
    public DepotStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(DepotStationBlockEntity.TOTAL_SLOTS));
    }

    // Server-side constructor
    public DepotStationMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.DEPOT_STATION_MENU.get(), containerId);
        checkContainerSize(container, DepotStationBlockEntity.TOTAL_SLOTS);
        this.container = container;
        this.access = ContainerLevelAccess.NULL;

        container.startOpen(playerInventory.player);

        // Slot 0: Fuel Slot
        this.addSlot(new Slot(container, DepotStationBlockEntity.SLOT_FUEL, 26, 20));

        // Slot 1: Hoe Slot
        this.addSlot(new Slot(container, DepotStationBlockEntity.SLOT_HOE, 26, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof HoeItem;
            }
        });

        // Slot 2: Upgrade Slot
        this.addSlot(new Slot(container, DepotStationBlockEntity.SLOT_UPGRADE, 26, 70) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof RangeUpgradeItem;
            }
        });

        // Slots 3..11: Output Grid (3x3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = DepotStationBlockEntity.OUTPUT_SLOT_START + col + row * 3;
                this.addSlot(new Slot(container, slotIndex, 98 + col * 18, 20 + row * 18));
            }
        }

        // Player Inventory (3 rows x 9 cols)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 98 + row * 18));
            }
        }

        // Player Hotbar (9 cols)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 156));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack current = slot.getItem();
            itemstack = current.copy();

            if (index < DepotStationBlockEntity.TOTAL_SLOTS) {
                // Moving from depot to player inventory
                if (!this.moveItemStackTo(current, DepotStationBlockEntity.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory into depot
                if (current.getItem() instanceof HoeItem) {
                    if (!this.moveItemStackTo(current, DepotStationBlockEntity.SLOT_HOE, DepotStationBlockEntity.SLOT_HOE + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (current.getItem() instanceof RangeUpgradeItem) {
                    if (!this.moveItemStackTo(current, DepotStationBlockEntity.SLOT_UPGRADE, DepotStationBlockEntity.SLOT_UPGRADE + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(current, DepotStationBlockEntity.SLOT_FUEL, DepotStationBlockEntity.SLOT_FUEL + 1, false)) {
                    // Try output slots
                    if (!this.moveItemStackTo(current, DepotStationBlockEntity.OUTPUT_SLOT_START, DepotStationBlockEntity.TOTAL_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (current.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
