package net.kevineleven.undertale_healthbars.mixin;


import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.damageInfos;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Unique
    float oldHealth = 0.0f;

    @Inject(method = "tick", at = @At("TAIL"))
    private void entityTick(CallbackInfo ci) {

        LivingEntity entity = (LivingEntity) (Object) this;
        if ((entity.getWorld().isClient)) {

            PlayerEntity player = UndertaleHealthBarsClient.client.player;
            float newHealth = entity.getHealth();
            if (newHealth != oldHealth) {
                if (oldHealth != 0.0) {
                    float damage = oldHealth - newHealth;

                    if (damage < 0) {
//                        player.sendMessage(Text.of(entityName + " healed " + Math.abs(damage) + "hp, Health now: " + entity.getHealth()));
                    } else {
//                        player.sendMessage(Text.of(entityName + " damaged " + damage + "hp, Health now: " + entity.getHealth()));
                    }
                    damageInfos.put(entity, new DamageInfo(damage, 20, 0.23f));
                }
                oldHealth = newHealth;
            }

            if (damageInfos.containsKey(entity)) {
                damageInfos.get(entity).timer --;
                //player.sendMessage(Text.of(entity.getName().getString() + " damage num tick, timer: " + damageInfos.get(entity).timer));

                if (damageInfos.get(entity).timer <= 0) {
                    damageInfos.remove(entity);
                } else {
                    damageInfos.get(entity).y_offset += damageInfos.get(entity).y_velocity;
                    damageInfos.get(entity).y_offset = Math.max(damageInfos.get(entity).y_offset, 0.0f);
                    damageInfos.get(entity).y_velocity -= DamageInfo.GRAVITY;
                }
            }
        }
    }
}
