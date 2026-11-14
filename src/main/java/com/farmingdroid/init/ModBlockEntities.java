package com.farmingdroid.init;

import com.farmingdroid.FarmingDroid;
import com.farmingdroid.block.entity.DepotStationBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FarmingDroid.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DepotStationBlockEntity>> DEPOT_STATION =
            BLOCK_ENTITIES.register("depot_station", () ->
                    BlockEntityType.Builder.of(DepotStationBlockEntity::new, ModBlocks.DEPOT_STATION.get())
                            .build(null));
}
