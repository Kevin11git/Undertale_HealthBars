package net.kevineleven.undertale_healthbars.event;


import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;

import java.util.HashMap;
import java.util.Map;

import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.bossDamageInfos;
import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.damageInfos;

public class EndClientTickEvent implements ClientTickEvents.EndTick {
    @Override
    public void onEndTick(MinecraftClient client) {
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

        Map<BossBar, DamageInfo> bossDamageInfosCopy = new HashMap<>(bossDamageInfos);
        for (BossBar bossBar : bossDamageInfosCopy.keySet()) {
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
