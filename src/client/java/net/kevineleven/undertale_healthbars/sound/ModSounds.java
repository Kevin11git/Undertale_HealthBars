package net.kevineleven.undertale_healthbars.sound;

import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    private ModSounds() {
        // private empty constructor to avoid accidental instantiation
    }

    public static final SoundEvent DAMAGE = registerSound("snd_damage");
    public static final SoundEvent HEAL = registerSound("snd_heal");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of(UndertaleHealthBarsClient.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void initialize() {
        UndertaleHealthBarsClient.LOGGER.info("Registering " + UndertaleHealthBarsClient.MOD_ID + " Sounds");
    }
}
