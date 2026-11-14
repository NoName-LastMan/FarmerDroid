package com.farmingdroid.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum DroidState {
    DOCKED_IDLE("docked_idle", Component.translatable("state.farmingdroid.docked").withStyle(ChatFormatting.GREEN)),
    SCANNING("scanning", Component.translatable("state.farmingdroid.scanning").withStyle(ChatFormatting.YELLOW)),
    WORKING("working", Component.translatable("state.farmingdroid.working").withStyle(ChatFormatting.AQUA)),
    RETURNING("returning", Component.translatable("state.farmingdroid.returning").withStyle(ChatFormatting.BLUE)),
    STALLED("stalled", Component.translatable("state.farmingdroid.stalled").withStyle(ChatFormatting.RED));

    private final String name;
    private final Component displayName;

    DroidState(String name, Component displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return displayName;
    }
}
