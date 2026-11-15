package com.farmingdroid.init;

import com.farmingdroid.FarmingDroid;
import com.farmingdroid.item.FarmingDroidItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FarmingDroid.MOD_ID);

    // Specific Droid item
    public static final DeferredItem<FarmingDroidItem> FARMING_DROID_ITEM =
            ITEMS.register("farming_droid", () -> new FarmingDroidItem(new Item.Properties()));

    // Specific Depot block item
    public static final DeferredItem<BlockItem> DEPOT_STATION_ITEM =
            ITEMS.register("depot_station", () -> new BlockItem(ModBlocks.DEPOT_STATION.get(), new Item.Properties()));
}
