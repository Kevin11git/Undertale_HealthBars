package net.kevineleven.undertale_healthbars.keybind;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;

public class ModKeybinds {
    // CATEGORIES
    private static final KeyMapping.Category UNDERTALE_HEALTHBARS_CATEGORY = registerCategory("undertale_healthbars");

    // KEYBINDS
    public static final KeyMapping TOGGLE_MOD = registerKeybind(
            "key.undertale_healthbars.toggleMod", InputConstants.UNKNOWN.getValue(),
            UNDERTALE_HEALTHBARS_CATEGORY);

    public static final KeyMapping OPEN_CONFIG = registerKeybind(
            "key.undertale_healthbars.openConfig", InputConstants.UNKNOWN.getValue(),
            UNDERTALE_HEALTHBARS_CATEGORY);



    private static KeyMapping registerKeybind(String name, int default_key, KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                name, // The translation key of the keybinding's name
                InputConstants.Type.KEYBOARD, // The type of the keybinding, KEYBOARD for keyboard, MOUSE for mouse.
                default_key, // The keycode of the key
                category // The translation key of the keybinding's category.
        ));
    }
    private static KeyMapping.Category registerCategory(String name) {
        return KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(UndertaleHealthBarsClient.MOD_ID, name)
        );
    }

    public static void initialize() {
        UndertaleHealthBarsClient.LOGGER.info("Registering " + UndertaleHealthBarsClient.MOD_ID + " Keybinds");
    }
}
