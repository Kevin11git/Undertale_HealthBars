package net.kevineleven.undertale_healthbars.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerAttackMixin {
    // THIS IS DISABLED FOR NOW

    // float k = this.getKnockbackAgainst(target, damageSource) + (bl2 ? 1.0F : 0.0F);
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void playerAttack(Entity target, CallbackInfo ci, @Local(ordinal = 3) float i) {
        if (i == 0.0f) {return;}

        PlayerEntity player = (PlayerEntity) (Object) this;
        LivingEntity playerLiving =  (LivingEntity) player;
        LivingEntity targetLiving = ((LivingEntity) target);

        player.sendMessage(Text.of("Entity Damage: " + i + ", Health: " + targetLiving.getHealth()));
    }

}
