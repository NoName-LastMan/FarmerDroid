package com.farmingdroid.block.entity;

import com.droids.core.block.entity.AbstractDepotBlockEntity;
import com.farmingdroid.init.ModBlockEntities;
import com.farmingdroid.menu.DepotStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class DepotStationBlockEntity extends AbstractDepotBlockEntity {

    public DepotStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DEPOT_STATION.get(), pos, blockState);
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
