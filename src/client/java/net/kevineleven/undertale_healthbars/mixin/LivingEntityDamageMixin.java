package net.kevineleven.undertale_healthbars.mixin;


import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.sound.ModSounds;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;
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
        if (!(entity.getEntityWorld().isClient())) {
            return;
        }
        if (!shouldRenderForLivingEntity(entity)) {
            return;
        }
        MinecraftClient client = UndertaleHealthBarsClient.client;


        float newHealth = entity.getHealth();
        if (newHealth != oldHealth) {
            if (oldHealth != -473.113f) {
                boolean isCurrentPlayer = false;
                if (entity instanceof PlayerEntity playerEntity) {
                    if (UndertaleHealthBarsClient.client.player.equals(playerEntity)) {
                        isCurrentPlayer = true;
                    }
                }

                float damage = oldHealth - newHealth;
                SoundManager soundManager = client.getSoundManager();

                if (ModConfig.modEnabled) {
                    if (damage < 0 && (!isCurrentPlayer || ModConfig.healSoundForYourself)) {
                        soundManager.play(new PositionedSoundInstance(
                                ModSounds.HEAL,
                                SoundCategory.MASTER,
                                (float) (ModConfig.healSoundVolume) / 100, 1f, Random.create(),
                                entity.getBlockPos()
                        ));
                    } else if (damage > 0 && (!isCurrentPlayer || ModConfig.damageSoundForYourself)) {
                        soundManager.play(new PositionedSoundInstance(
                                ModSounds.DAMAGE,
                                SoundCategory.MASTER,
                                (float) (ModConfig.damageSoundVolume) / 100, 1f, Random.create(),
                                entity.getBlockPos()
                        ));
                    }
                }


                if (
                        (damage < 0 && ModConfig.showHealNumbers) ||
                        (damage > 0 && ModConfig.showDamageNumbers)
                ) {
                    damageInfos.put(entity, new DamageInfo(damage, 20, 0.23f));
                }
            }
            oldHealth = newHealth;
        }

    }



    @Unique
    private boolean shouldRenderForLivingEntity(LivingEntity livingEntity) {
        return !livingEntity.isInvisibleTo(UndertaleHealthBarsClient.client.player) &&
                (livingEntity.isAlive() || previousHealths.containsKey(livingEntity))
                ;
    }
}
