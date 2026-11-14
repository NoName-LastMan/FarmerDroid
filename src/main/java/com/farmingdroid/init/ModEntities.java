package com.farmingdroid.init;

import com.farmingdroid.FarmingDroid;
import com.farmingdroid.entity.FarmingDroidEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, FarmingDroid.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FarmingDroidEntity>> FARMING_DROID =
            ENTITY_TYPES.register("farming_droid", () ->
                    EntityType.Builder.of(FarmingDroidEntity::new, MobCategory.MISC)
                            .sized(0.6F, 0.8F)
                            .clientTrackingRange(10)
                            .build("farming_droid"));
}
