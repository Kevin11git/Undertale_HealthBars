package net.kevineleven.undertale_healthbars.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kevineleven.undertale_healthbars.event.EndClientTickEvent;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UndertaleHealthBarsClient implements ClientModInitializer {
    public static final String MOD_ID = "undertale_healthbars";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinecraftClient client = MinecraftClient.getInstance();


    public static Map<LivingEntity, Float> previousHealths = new HashMap<>();
    public static Map<LivingEntity, DamageInfo> damageInfos = new HashMap<>();

    public static Map<BossBar, Float> bossPreviousHealths = new HashMap<>();
    public static Map<BossBar, DamageInfo> bossDamageInfos = new HashMap<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Undertale healthbars mod loaded!");

        ClientTickEvents.END_CLIENT_TICK.register(new EndClientTickEvent());
    }
}
