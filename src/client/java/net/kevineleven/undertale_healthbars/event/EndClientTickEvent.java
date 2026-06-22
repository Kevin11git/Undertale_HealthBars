package net.kevineleven.undertale_healthbars.event;


import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.keybind.ModKeybinds;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.bossDamageInfos;
import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.damageInfos;

public class EndClientTickEvent implements ClientTickEvents.EndTick {
    @Override
    public void onEndTick(Minecraft client) {
        // Keybinds
        if (ModKeybinds.TOGGLE_MOD.consumeClick()) {
            ModConfig.modEnabled.set(!ModConfig.modEnabled.get());
            ModConfig.HANDLER.save();

            UndertaleHealthBarsClient.client.gui.hud.setOverlayMessage(Component.literal("Undertale HealthBars Mod ")
                    .append(ModConfig.modEnabled.get() ?
                    Component.literal("Enabled").withStyle(ChatFormatting.GREEN) :
                    Component.literal("Disabled").withStyle(ChatFormatting.RED))
                    .append("!")
            , false);
        }

        if (ModKeybinds.OPEN_CONFIG.consumeClick()) {
            Screen parent = UndertaleHealthBarsClient.client.gui.screen();
            UndertaleHealthBarsClient.client.gui.setScreen(ModConfig.getScreen(parent));
        }


        // damage info jumping and timer
        Map<LivingEntity, DamageInfo> damageInfosCopy = new HashMap<>(damageInfos);
        for (LivingEntity entity : damageInfosCopy.keySet()) {
            damageInfos.get(entity).timer--;

            if (damageInfos.get(entity).timer <= 0) {
                damageInfos.remove(entity);
            } else {
                damageInfos.get(entity).y_offset += damageInfos.get(entity).y_velocity;
                damageInfos.get(entity).y_offset = Math.max(damageInfos.get(entity).y_offset, 0.0f);
                damageInfos.get(entity).y_velocity -= DamageInfo.GRAVITY;
            }
        }

        Map<BossEvent, DamageInfo> bossDamageInfosCopy = new HashMap<>(bossDamageInfos);
        for (BossEvent bossBar : bossDamageInfosCopy.keySet()) {
            bossDamageInfos.get(bossBar).timer--;

            if (bossDamageInfos.get(bossBar).timer <= 0) {
                bossDamageInfos.remove(bossBar);
            } else {
                bossDamageInfos.get(bossBar).y_offset += bossDamageInfos.get(bossBar).y_velocity;
                bossDamageInfos.get(bossBar).y_offset = Math.max(bossDamageInfos.get(bossBar).y_offset, 0.0f);
                bossDamageInfos.get(bossBar).y_velocity -= DamageInfo.GRAVITY;
            }
        }
    }
}
