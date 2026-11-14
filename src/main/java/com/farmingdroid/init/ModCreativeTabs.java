package com.farmingdroid.init;

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
                    .icon(() -> new ItemStack(ModItems.DROID_CORE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.DROID_CORE.get());
                        output.accept(ModItems.FARMING_REMOTE.get());
                    })
                    .build());
}
