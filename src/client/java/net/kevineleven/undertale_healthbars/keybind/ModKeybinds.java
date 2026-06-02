package net.kevineleven.undertale_healthbars.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    // CATEGORIES
    private static final KeyMapping.Category UNDERTALE_HEALTHBARS_CATEGORY = registerCategory("undertale_healthbars");

    // KEYBINDS
    public static final KeyMapping TOGGLE_MOD = registerKeybind(
            "key.undertale_healthbars.toggleMod", GLFW.GLFW_KEY_UNKNOWN,
            UNDERTALE_HEALTHBARS_CATEGORY);

    public static final KeyMapping OPEN_CONFIG = registerKeybind(
            "key.undertale_healthbars.openConfig", GLFW.GLFW_KEY_UNKNOWN,
            UNDERTALE_HEALTHBARS_CATEGORY);



    private static KeyMapping registerKeybind(String name, int default_key, KeyMapping.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                name, // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
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
