package net.kevineleven.undertale_healthbars.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LerpingBossEvent.class)
public class LerpingBossEventMixin {

    @WrapOperation(
            method = "getProgress",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F")
    )
    private float removeLerpForEntityBossBars(float value, float min, float max, Operation<Float> original) {
        LerpingBossEvent bossBar = (LerpingBossEvent) (Object) this;
        if (isBossBarOfEntity(bossBar) &&
           (ModConfig.showUndertaleBossbars.get() || ModConfig.showUndertaleBossbarDamageNumbers.get() || ModConfig.showUndertaleBossbarHealNumbers.get()) &&
                ModConfig.modEnabled.get()
        ) {
            return 1f;
        } else {
            return original.call(value, min , max);
        }
    }

    @Unique
    private boolean isBossBarOfEntity(BossEvent bossBar) {
        if (
                bossBar.getName().getString().equals("Ender Dragon") ||
                        bossBar.getName().getString().equals("Wither")
        ) {
            return true;
        }

        return false;
    }
}
