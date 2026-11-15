package com.farmingdroid.client;

import com.farmingdroid.FarmingDroid;
import com.farmingdroid.client.renderer.FarmingDroidModel;
import com.farmingdroid.client.renderer.FarmingDroidRenderer;
import com.farmingdroid.init.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = FarmingDroid.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FARMING_DROID.get(), FarmingDroidRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FarmingDroidRenderer.LAYER_LOCATION, FarmingDroidModel::createBodyLayer);
    }
}
