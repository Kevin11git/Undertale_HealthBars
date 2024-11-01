package net.kevineleven.undertale_healthbars.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndertaleHealthBarsClient implements ClientModInitializer {
    public static final String MOD_ID = "undertale_healthbars";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinecraftClient client = MinecraftClient.getInstance();



    @Override
    public void onInitializeClient() {
        LOGGER.info("Undertale healthbars mod loaded!");

//        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
//            if (entity.isAlive()) {
//                LivingEntity livingEntity = ((LivingEntity) entity);
//                player.sendMessage(Text.of("Entity Health: " + livingEntity.getHealth()));
//                return ActionResult.PASS;
//            }
//
//            return ActionResult.PASS;
//        });
    }
}
