package net.kevineleven.undertale_healthbars.mixin;


import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.sound.ModSounds;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.damageInfos;
import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.previousHealths;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Unique
    float oldHealth = -473.113f;

    @Inject(method = "tick", at = @At("RETURN"))
    private void entityTick(CallbackInfo ci) {

        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity.level().isClientSide())) {
            return;
        }
        if (!shouldRenderForLivingEntity(entity)) {
            return;
        }
        Minecraft client = UndertaleHealthBarsClient.client;


        float newHealth = entity.getHealth();
        if (newHealth != oldHealth) {
            if (oldHealth != -473.113f) {
                boolean isCurrentPlayer = false;
                if (entity instanceof Player playerEntity) {
                    if (UndertaleHealthBarsClient.client.player.equals(playerEntity)) {
                        isCurrentPlayer = true;
                    }
                }

                float damage = oldHealth - newHealth;
                SoundManager soundManager = client.getSoundManager();

                if (ModConfig.modEnabled.get()) {
                    // Playing snd_heal
                    if (damage < 0 && (!isCurrentPlayer || ModConfig.healSoundForYourself.get()) && ModConfig.healSoundVolume.get() > 0  && (isCurrentPlayer || !entity.isInvisibleTo(UndertaleHealthBarsClient.client.player))) {
                        soundManager.play(new SimpleSoundInstance(
                                ModSounds.HEAL,
                                SoundSource.MASTER,
                                (float) (ModConfig.healSoundVolume.get()) / 100, 1f, RandomSource.create(),
                                entity.blockPosition()
                        ));
                    // Playing snd_damage
                    } else if (damage > 0 && (!isCurrentPlayer || ModConfig.damageSoundForYourself.get()) && ModConfig.damageSoundVolume.get() > 0) {
                        soundManager.play(new SimpleSoundInstance(
                                ModSounds.DAMAGE,
                                SoundSource.MASTER,
                                (float) (ModConfig.damageSoundVolume.get()) / 100, 1f, RandomSource.create(),
                                entity.blockPosition()
                        ));
                    }

                    // Playing snd_vaporized
                    if (newHealth == 0f && (!isCurrentPlayer || ModConfig.vaporizedSoundForYourself.get()) && ModConfig.vaporizedSoundVolume.get() > 0) {
                        soundManager.play(new SimpleSoundInstance(
                                ModSounds.VAPORIZED,
                                SoundSource.MASTER,
                                (float) (ModConfig.vaporizedSoundVolume.get()) / 100, 1f, RandomSource.create(),
                                entity.blockPosition()
                        ));
                    }
                }


                if (
                        (damage < 0 && ModConfig.showHealNumbers.get()) ||
                        (damage > 0 && ModConfig.showDamageNumbers.get())
                ) {
                    damageInfos.put(entity, new DamageInfo(damage, (int)(20f * ModConfig.damageHealNumbersShowDuration.get()), 0.23f));
                }
            }
            oldHealth = newHealth;
        }

    }



    @Unique
    private boolean shouldRenderForLivingEntity(LivingEntity livingEntity) {
        return (livingEntity.isAlive() || previousHealths.containsKey(livingEntity))
                ;
    }
}
