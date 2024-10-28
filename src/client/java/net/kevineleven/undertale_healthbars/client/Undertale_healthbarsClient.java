package net.kevineleven.undertale_healthbars.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Undertale_healthbarsClient implements ClientModInitializer {
    public static final String MOD_ID = "undertale_healthbars";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    @Override
    public void onInitializeClient() {
        LOGGER.info("Undertale healthbars mod loaded!");
    }
}
