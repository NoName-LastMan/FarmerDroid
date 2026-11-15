package com.farmingdroid.menu;

import com.droids.core.block.entity.AbstractDepotBlockEntity;
import com.droids.core.menu.AbstractDepotMenu;
import com.farmingdroid.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

public class DepotStationMenu extends AbstractDepotMenu {

    // Client-side constructor
    public DepotStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(AbstractDepotBlockEntity.TOTAL_SLOTS));
    }

    // Server-side constructor
    public DepotStationMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.DEPOT_STATION_MENU.get(), containerId, playerInventory, container);
    }

    @Override
    public boolean isToolItem(ItemStack stack) {
        return stack.is(ItemTags.HOES) || stack.getItem() instanceof HoeItem;
    }
}
