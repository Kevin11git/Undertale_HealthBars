package net.kevineleven.undertale_healthbars.mixin;


import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.sound.ModSounds;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
                if (ModConfig.modEnabled.get()) {
                    // Playing snd_heal
                    if (damage < 0 && (!isCurrentPlayer || ModConfig.healSoundForYourself.get()) && ModConfig.healSoundVolume.get() > 0  && (isCurrentPlayer || !entity.isInvisibleTo(UndertaleHealthBarsClient.client.player))) {
                        ModSounds.playSound(
                                ModSounds.HEAL,
                                (float) (ModConfig.healSoundVolume.get()) / 100,
                                1f,
                                entity.blockPosition()
                        );
                    // Playing snd_damage
                    } else if (damage > 0 && (!isCurrentPlayer || ModConfig.damageSoundForYourself.get()) && ModConfig.damageSoundVolume.get() > 0) {
                        ModSounds.playSound(
                                ModSounds.DAMAGE,
                                (float) (ModConfig.damageSoundVolume.get()) / 100,
                                1f,
                                entity.blockPosition()
                        );
                    }

                    // Playing snd_vaporized
                    if (newHealth == 0f && (!isCurrentPlayer || ModConfig.vaporizedSoundForYourself.get()) && ModConfig.vaporizedSoundVolume.get() > 0) {
                        ModSounds.playSound(
                                ModSounds.VAPORIZED,
                                (float) (ModConfig.vaporizedSoundVolume.get()) / 100,
                                1f,
                                entity.blockPosition()
                        );
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
