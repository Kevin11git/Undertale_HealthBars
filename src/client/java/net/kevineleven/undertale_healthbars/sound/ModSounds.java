package net.kevineleven.undertale_healthbars.sound;

import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;

public class ModSounds {
    private ModSounds() {
        // private empty constructor to avoid accidental instantiation
    }

    public static final SoundEvent DAMAGE = registerSound("snd_damage");
    public static final SoundEvent HEAL = registerSound("snd_heal");
    public static final SoundEvent VAPORIZED = registerSound("snd_vaporized");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(UndertaleHealthBarsClient.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {
        UndertaleHealthBarsClient.LOGGER.info("Registering " + UndertaleHealthBarsClient.MOD_ID + " Sounds");
    }
}
