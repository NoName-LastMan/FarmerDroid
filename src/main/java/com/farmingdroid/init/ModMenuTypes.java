package com.farmingdroid.init;

import com.farmingdroid.FarmingDroid;
import com.farmingdroid.menu.DepotStationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, FarmingDroid.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DepotStationMenu>> DEPOT_STATION_MENU =
            MENUS.register("depot_station_menu", () ->
                    IMenuTypeExtension.create(DepotStationMenu::new));
}
