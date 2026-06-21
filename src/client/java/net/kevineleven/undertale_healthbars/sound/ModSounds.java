package net.kevineleven.undertale_healthbars.sound;

import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class ModSounds {
    private ModSounds() {
        // private empty constructor to avoid accidental instantiation
    }

    public static final Identifier DAMAGE = registerSound("snd_damage");
    public static final Identifier LIGHT_DAMAGE = registerSound("snd_light_damage");
    public static final Identifier HEAVY_DAMAGE = registerSound("snd_heavy_damage");
    public static final Identifier HEAL = registerSound("snd_heal");
    public static final Identifier VAPORIZED = registerSound("snd_vaporized");

    private static Identifier registerSound(String id) {
        return Identifier.fromNamespaceAndPath(UndertaleHealthBarsClient.MOD_ID, id);
    }

    public static void initialize() {
        UndertaleHealthBarsClient.LOGGER.info("Registering " + UndertaleHealthBarsClient.MOD_ID + " Sounds");
    }

    public static SoundEngine.PlayResult playSound(Identifier sound, float volume, float pitch, BlockPos location) {
        return UndertaleHealthBarsClient.client.getSoundManager().play(
                new SimpleSoundInstance(
                        SoundEvent.createFixedRangeEvent(
                                sound,
                                16f
                        ),
                        SoundSource.MASTER,
                        volume,
                        pitch,
                        RandomSource.create(),
                        location
                )
        );
    }
}
