package com.farmingdroid.init;

import com.farmingdroid.FarmingDroid;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FarmingDroid.MOD_ID);

    // Items for Farming Droid
    public static final DeferredItem<Item> DROID_CORE = ITEMS.registerSimpleItem("droid_core", new Item.Properties());
    public static final DeferredItem<Item> FARMING_REMOTE = ITEMS.registerSimpleItem("farming_remote", new Item.Properties().stacksTo(1));
}
