package com.farmingdroid;

import com.mojang.logging.LogUtils;
import com.farmingdroid.init.ModCreativeTabs;
import com.farmingdroid.init.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(FarmingDroid.MOD_ID)
public class FarmingDroid {
    public static final String MOD_ID = "farmingdroid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FarmingDroid(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Farming Droid Mod (Steampunk Automation)...");

        // Register Deferred Registers
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Lifecycle event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register NeoForge event bus
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Farming Droid common setup finished.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Farming Droid client setup finished.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Farming Droid: Dedicated/Integrated server starting.");
    }
}
