package com.farmingdroid.item;

import com.farmingdroid.block.DepotStationBlock;
import com.farmingdroid.entity.FarmingDroidEntity;
import com.farmingdroid.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.getBlock() instanceof DepotStationBlock) {
            if (!level.isClientSide() && player != null) {
                // Save target depot pos into player's hand or tag
                player.sendSystemMessage(Component.translatable("message.farmingdroid.wrench_depot_selected",
                        pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GOLD));
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.8F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof FarmingDroidEntity droid) {
            Level level = droid.level();

            if (player.isShiftKeyDown()) {
                // Dismantle droid into item form
                if (!level.isClientSide()) {
                    ItemStack droidItem = new ItemStack(ModItems.FARMING_DROID_ITEM.get());
                    droid.writeToItemTag(droidItem);

                    if (!player.getInventory().add(droidItem)) {
                        droid.spawnAtLocation(droidItem);
                    }

                    level.playSound(null, droid.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS, 1.0F, 0.9F);
                    droid.discard();
                    player.sendSystemMessage(Component.translatable("message.farmingdroid.droid_picked_up").withStyle(ChatFormatting.GREEN));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                // Inspect status or link
                if (!level.isClientSide()) {
                    Component stateName = droid.getDroidState().getDisplayName();
                    int fuelPercent = droid.getFuelPercentage();
                    ItemStack currentHoe = droid.getHoeItem();
                    String hoeInfo = currentHoe.isEmpty()
                            ? Component.translatable("text.farmingdroid.none").getString()
                            : currentHoe.getHoverName().getString() + " (" + (currentHoe.getMaxDamage() - currentHoe.getDamageValue()) + "/" + currentHoe.getMaxDamage() + ")";

                    player.displayClientMessage(Component.translatable("message.farmingdroid.status_bar",
                            stateName, fuelPercent, hoeInfo).withStyle(ChatFormatting.AQUA), true);
                    level.playSound(null, droid.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.5F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.wrench_1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.wrench_2").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.wrench_3").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
