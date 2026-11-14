package com.farmingdroid.init;

import com.farmingdroid.FarmingDroid;
import com.farmingdroid.item.FarmingDroidItem;
import com.farmingdroid.item.RangeUpgradeItem;
import com.farmingdroid.item.WrenchItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FarmingDroid.MOD_ID);

    // Tools & Utility
    public static final DeferredItem<WrenchItem> WRENCH =
            ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties()));

    public static final DeferredItem<FarmingDroidItem> FARMING_DROID_ITEM =
            ITEMS.register("farming_droid", () -> new FarmingDroidItem(new Item.Properties()));

    // Upgrades (3x3 default, 5x5 tier 1, 7x7 tier 2, 9x9 tier 3)
    public static final DeferredItem<RangeUpgradeItem> RANGE_UPGRADE_1 =
            ITEMS.register("range_upgrade_1", () -> new RangeUpgradeItem(RangeUpgradeItem.RangeTier.BRASS, new Item.Properties()));

    public static final DeferredItem<RangeUpgradeItem> RANGE_UPGRADE_2 =
            ITEMS.register("range_upgrade_2", () -> new RangeUpgradeItem(RangeUpgradeItem.RangeTier.IRON, new Item.Properties()));

    public static final DeferredItem<RangeUpgradeItem> RANGE_UPGRADE_3 =
            ITEMS.register("range_upgrade_3", () -> new RangeUpgradeItem(RangeUpgradeItem.RangeTier.STEAM_OVERCLOCKED, new Item.Properties()));

    // Crafting Component
    public static final DeferredItem<Item> DROID_CORE =
            ITEMS.registerSimpleItem("droid_core", new Item.Properties());

    // Block Item
    public static final DeferredItem<BlockItem> DEPOT_STATION_ITEM =
            ITEMS.register("depot_station", () -> new BlockItem(ModBlocks.DEPOT_STATION.get(), new Item.Properties()));
}
