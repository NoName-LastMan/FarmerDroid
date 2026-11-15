package com.farmingdroid.item;

import com.droids.core.entity.AbstractDroidEntity;
import com.droids.core.item.AbstractDroidItem;
import com.farmingdroid.entity.FarmingDroidEntity;
import com.farmingdroid.init.ModEntities;
import net.minecraft.world.level.Level;

public class FarmingDroidItem extends AbstractDroidItem {
    public FarmingDroidItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractDroidEntity createDroidEntity(Level level) {
        return new FarmingDroidEntity(ModEntities.FARMING_DROID.get(), level);
    }
}
