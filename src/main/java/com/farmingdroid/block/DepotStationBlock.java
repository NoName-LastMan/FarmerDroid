package com.farmingdroid.block;

import com.droids.core.block.AbstractDepotBlock;
import com.farmingdroid.block.entity.DepotStationBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DepotStationBlock extends AbstractDepotBlock {
    public static final MapCodec<DepotStationBlock> CODEC = simpleCodec(DepotStationBlock::new);

    public DepotStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DepotStationBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DepotStationBlockEntity(pos, state);
    }
}
