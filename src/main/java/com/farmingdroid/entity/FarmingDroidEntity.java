package com.farmingdroid.entity;

import com.droids.core.entity.AbstractDroidEntity;
import com.farmingdroid.entity.ai.FarmingDroidHarvestGoal;
import com.farmingdroid.init.ModItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FarmingDroidEntity extends AbstractDroidEntity {

    public FarmingDroidEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FarmingDroidHarvestGoal(this, 1.0D));
    }

    @Override
    public boolean isMatchingTool(ItemStack stack) {
        return stack.is(ItemTags.HOES) || stack.getItem() instanceof HoeItem;
    }

    @Override
    public ItemStack createDismantledItem() {
        return new ItemStack(ModItems.FARMING_DROID_ITEM.get());
    }

    public ItemStack getHoeItem() {
        return getToolItem();
    }

    public void setHoeItem(ItemStack hoe) {
        setToolItem(hoe);
    }

    public boolean hasHoe() {
        return hasTool();
    }

    public void damageHoe(int amount) {
        damageTool(amount);
    }
}
