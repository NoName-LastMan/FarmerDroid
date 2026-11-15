package com.farmingdroid.init;

import com.droids.core.init.CoreItems;
import com.farmingdroid.FarmingDroid;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FarmingDroid.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FARMING_DROID_TAB =
            CREATIVE_MODE_TABS.register("farmingdroid_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.farmingdroid"))
                    .icon(() -> new ItemStack(ModItems.FARMING_DROID_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(CoreItems.STEAMPUNK_WRENCH.get());
                        output.accept(ModItems.FARMING_DROID_ITEM.get());
                        output.accept(CoreItems.DEPOT_STATION.get());
                        output.accept(CoreItems.RANGE_UPGRADE_TIER_1.get());
                        output.accept(CoreItems.RANGE_UPGRADE_TIER_2.get());
                        output.accept(CoreItems.RANGE_UPGRADE_TIER_3.get());
                        output.accept(CoreItems.DROID_CORE.get());
                    })
                    .build());
}
