package com.farmingdroid.init;

import com.farmingdroid.FarmingDroid;
import com.farmingdroid.block.DepotStationBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FarmingDroid.MOD_ID);

    public static final DeferredBlock<DepotStationBlock> DEPOT_STATION = BLOCKS.register("depot_station",
            () -> new DepotStationBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()));
}
