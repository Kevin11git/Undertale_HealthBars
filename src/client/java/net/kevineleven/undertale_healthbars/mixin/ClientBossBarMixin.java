package net.kevineleven.undertale_healthbars.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientBossBar.class)
public class ClientBossBarMixin {

    @WrapOperation(
            method = "getPercent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F")
    )
    private float removeLerpForEntityBossBars(float value, float min, float max, Operation<Float> original) {
        ClientBossBar bossBar = (ClientBossBar) (Object) this;
        if (isBossBarOfEntity(bossBar) &&
           (ModConfig.showUndertaleBossbars || ModConfig.showUndertaleBossbarDamageNumbers || ModConfig.showUndertaleBossbarHealNumbers) &&
                ModConfig.modEnabled
        ) {
            return 1f;
        } else {
            return original.call(value, min , max);
        }
    }

    @Unique
    private boolean isBossBarOfEntity(BossBar bossBar) {
        if (
                bossBar.getName().getString().equals("Ender Dragon") ||
                        bossBar.getName().getString().equals("Wither")
        ) {
            return true;
        }

        return false;
    }
}
