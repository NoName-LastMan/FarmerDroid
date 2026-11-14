package com.farmingdroid.client.renderer;

import com.farmingdroid.entity.FarmingDroidEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FarmingDroidRenderer extends MobRenderer<FarmingDroidEntity, FarmingDroidModel> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("farmingdroid", "farming_droid"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("farmingdroid", "textures/entity/farming_droid.png");

    public FarmingDroidRenderer(EntityRendererProvider.Context context) {
        super(context, new FarmingDroidModel(context.bakeLayer(LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(FarmingDroidEntity entity) {
        return TEXTURE;
    }
}
