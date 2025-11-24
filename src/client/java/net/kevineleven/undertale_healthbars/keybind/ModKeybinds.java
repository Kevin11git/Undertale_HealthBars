package net.kevineleven.undertale_healthbars.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    // CATEGORIES
    private static final KeyBinding.Category UNDERTALE_HEALTHBARS_CATEGORY = registerCategory("undertale_healthbars");

    // KEYBINDS
    public static final KeyBinding TOGGLE_MOD = registerKeybind(
            "key.undertale_healthbars.toggleMod", GLFW.GLFW_KEY_UNKNOWN,
            UNDERTALE_HEALTHBARS_CATEGORY);

    public static final KeyBinding OPEN_CONFIG = registerKeybind(
            "key.undertale_healthbars.openConfig", GLFW.GLFW_KEY_UNKNOWN,
            UNDERTALE_HEALTHBARS_CATEGORY);



    private static KeyBinding registerKeybind(String name, int default_key, KeyBinding.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                name, // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                default_key, // The keycode of the key
                category // The translation key of the keybinding's category.
        ));
    }
    private static KeyBinding.Category registerCategory(String name) {
        return KeyBinding.Category.create(
                Identifier.of(UndertaleHealthBarsClient.MOD_ID, name)
        );
    }

    public static void initialize() {
        UndertaleHealthBarsClient.LOGGER.info("Registering " + UndertaleHealthBarsClient.MOD_ID + " Keybinds");
    }
}
