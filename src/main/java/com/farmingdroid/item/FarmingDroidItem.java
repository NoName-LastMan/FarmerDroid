package com.farmingdroid.item;

import com.farmingdroid.block.DepotStationBlock;
import com.farmingdroid.entity.DroidState;
import com.farmingdroid.entity.FarmingDroidEntity;
import com.farmingdroid.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

public class FarmingDroidItem extends Item {
    public FarmingDroidItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Player player = context.getPlayer();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Farming Droid can ONLY be placed on the top face of a Depot Station
        if (!(clickedState.getBlock() instanceof DepotStationBlock) || clickedFace != Direction.UP) {
            if (!level.isClientSide() && player != null) {
                player.displayClientMessage(
                        Component.translatable("message.farmingdroid.place_on_depot_only").withStyle(ChatFormatting.RED),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        BlockPos abovePos = clickedPos.above();
        if (!level.getBlockState(abovePos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            ItemStack itemInHand = context.getItemInHand();
            FarmingDroidEntity droid = new FarmingDroidEntity(ModEntities.FARMING_DROID.get(), level);

            // Position droid centered on top of depot
            droid.moveTo(clickedPos.getX() + 0.5D, clickedPos.getY() + 0.9D, clickedPos.getZ() + 0.5D, context.getRotation(), 0.0F);

            // Restore any stored NBT data from item
            droid.readFromItemTag(itemInHand);

            // Automatically link and dock to this depot station
            droid.setLinkedDepotPos(clickedPos);
            droid.setDroidState(DroidState.DOCKED_IDLE);

            level.addFreshEntity(droid);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, abovePos);
            level.playSound(null, clickedPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.7F, 1.2F);

            if (player != null && !player.getAbilities().instabuild) {
                itemInHand.shrink(1);
            }

            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.farmingdroid.droid_deployed").withStyle(ChatFormatting.GREEN),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.droid_desc").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.droid_placement_rule").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.droid_fuel_type").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
