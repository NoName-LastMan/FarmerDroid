package com.farmingdroid.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class RangeUpgradeItem extends Item {
    public enum RangeTier {
        BRASS(2, "5x5", ChatFormatting.GOLD),
        IRON(3, "7x7", ChatFormatting.AQUA),
        STEAM_OVERCLOCKED(4, "9x9", ChatFormatting.RED);

        private final int radius;
        private final String areaText;
        private final ChatFormatting color;

        RangeTier(int radius, String areaText, ChatFormatting color) {
            this.radius = radius;
            this.areaText = areaText;
            this.color = color;
        }

        public int getRadius() {
            return radius;
        }

        public String getAreaText() {
            return areaText;
        }

        public ChatFormatting getColor() {
            return color;
        }
    }

    private final RangeTier tier;

    public RangeUpgradeItem(RangeTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public RangeTier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.upgrade_range",
                Component.literal(tier.getAreaText()).withStyle(tier.getColor()))
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.farmingdroid.upgrade_instruction")
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
