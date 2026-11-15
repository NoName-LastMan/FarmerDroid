package com.farmingdroid.client.screen;

import com.droids.core.client.screen.AbstractDepotScreen;
import com.farmingdroid.menu.DepotStationMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DepotStationScreen extends AbstractDepotScreen<DepotStationMenu> {
    public DepotStationScreen(DepotStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
