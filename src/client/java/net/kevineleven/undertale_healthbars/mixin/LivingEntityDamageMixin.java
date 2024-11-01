package net.kevineleven.undertale_healthbars.mixin;


import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Unique
    float oldHealth = 0.0f;

    @Inject(method = "tick", at = @At("TAIL"))
    private void entityTick(CallbackInfo ci) {

        LivingEntity entity = (LivingEntity) (Object) this;
        if ((entity.getWorld().isClient)) {

            float newHealth = entity.getHealth();
            if (newHealth != oldHealth) {

                if (oldHealth != 0.0) {
                    float damage = oldHealth - newHealth;
                    PlayerEntity player = UndertaleHealthBarsClient.client.player;

                    String entityName;
                    entityName = entity.getName().getString();

                    if (damage < 0) {
                        player.sendMessage(Text.of(entityName + " healed " + Math.abs(damage) + "hp, Health now: " + entity.getHealth()));
                    } else {
                        player.sendMessage(Text.of(entityName + " damaged " + damage + "hp, Health now: " + entity.getHealth()));
                    }
                }
                oldHealth = newHealth;

            }
        }
    }
}
