package net.kevineleven.undertale_healthbars.mixin;


import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.*;


@Mixin(BossBarHud.class)
public class BossBarHudMixin {


    @Shadow @Final private static Identifier[] BACKGROUND_TEXTURES;

    @Shadow @Final private static int WIDTH;
    @Shadow @Final private static int HEIGHT;
    @Shadow @Final private static Identifier[] PROGRESS_TEXTURES;
    @Unique
    Map<BossBar, Float> oldHealths = new HashMap<>(); // for damage info

    @Inject(
            method = "renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;I[Lnet/minecraft/util/Identifier;[Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "RETURN")
    )
    private void renderBossBar(DrawContext context, int x, int y, BossBar bossBar, int width, Identifier[] textures, Identifier[] notchedTextures, CallbackInfo ci) {
        if (!(ModConfig.modEnabled)) {
            return;
        }

        if (isBossBarOfEntity(bossBar)) {
            int rectangleHeight = HEIGHT;

            if (!bossPreviousHealths.containsKey(bossBar)) {
                bossPreviousHealths.put(bossBar, bossBar.getPercent());
            }
            if (!oldHealths.containsKey(bossBar)) {
                oldHealths.put(bossBar, bossBar.getPercent() * getMaxHealthFromBossBar(bossBar));
            }

            float previousHealth = bossPreviousHealths.get(bossBar);

            if (Arrays.equals(textures, BACKGROUND_TEXTURES)){
                float oldHealth = oldHealths.get(bossBar);
                float newHealth = bossBar.getPercent() * getMaxHealthFromBossBar(bossBar);
                if (newHealth != oldHealth) {
                    float damage = oldHealth - newHealth;
                    if (
                            (damage < 0 && ModConfig.showUndertaleBossbarHealNumbers) ||
                            (damage > 0 && ModConfig.showUndertaleBossbarDamageNumbers)
                    ) {
                        bossDamageInfos.put(bossBar, new DamageInfo(damage, (int)(20f * ModConfig.bossbarsDamageHealNumbersShowDuration), 0.23f));
                    }

                    oldHealths.replace(bossBar, newHealth);

                    if (newHealth <= 0.0f) {
                        oldHealths.remove(bossBar);
                    }
                }



                if (previousHealth != bossBar.getPercent()) {

                    if (previousHealth == 1 && bossBar.getPercent() <= 0.0f) { // if died in 1 hit
                        bossPreviousHealths.replace(bossBar, -10f); // negative so bar doesn't disappear instantly
                    }
                    else {
                        bossPreviousHealths.replace(bossBar, MathHelper.lerp(0.1f, previousHealth, bossBar.getPercent()));
                    }

                    if (Math.abs(previousHealth - bossBar.getPercent()) < 0.0000001) {
                        bossPreviousHealths.replace(bossBar, bossBar.getPercent());
                    }

                    previousHealth = bossPreviousHealths.get(bossBar);
                }

                if (ModConfig.showUndertaleBossbars) {
                    // gray background rect
                    context.fill(x, y, x + width, y + rectangleHeight, 0xFF404040);
                }

            } else if (Arrays.equals(textures, PROGRESS_TEXTURES)) {
                if (ModConfig.showUndertaleBossbars) {
                    // green health rect
                    context.fill(x, y, x + ((int) Math.ceil(Math.max(0.0f, previousHealth) * WIDTH)), y + rectangleHeight, 0xFF00D600);
                }
                // damage numbers

                if (bossDamageInfos.containsKey(bossBar)) {
                    DamageInfo damageInfo = bossDamageInfos.get(bossBar);
                    float damage = damageInfo.damage;
                    String damage_or_heal = "damage";
                    if (damage < 0.0) {
                        damage_or_heal = "heal";
                        damage = Math.abs(damage);
                    }

                    if (
                            (damage_or_heal == "damage" && ModConfig.showUndertaleBossbarDamageNumbers) ||
                                    (damage_or_heal == "heal" && ModConfig.showUndertaleBossbarHealNumbers)
                    ) {

                        String textDamage;
                        if (Math.floor(damage) == damage) {
                            textDamage = String.format("%.0f", damage);
                        } else {
                            textDamage = String.format("%.2f", damage);
                        }

                        int number_x = x + WIDTH + 5;
                        Identifier texture;
                        float scale = 0.5f;
                        context.getMatrices().scale(scale, scale);
                        for (int index = 0; index < textDamage.length(); index++) {
                            char currentChar = textDamage.charAt(index);
                            if (currentChar == ',') {
                                currentChar = '.';
                            }
                            texture = Identifier.of(UndertaleHealthBarsClient.MOD_ID, "textures/ui/" + damage_or_heal + "_num_" + currentChar + ".png");
                            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, (int) (number_x * (1 / scale) + damageInfo.y_offset * 30), (int) ((y - Math.ceil(10 * scale)) * (1 / scale)), 0, 0, 30, 30, 30, 30);

                            number_x += (int) Math.ceil(31 * scale);
                        }
                        context.getMatrices().scale(1 / scale, 1 / scale);
                    }
                }

                if (ModConfig.showUndertaleBossbars) {
                    // black outline
                    context.drawStrokedRectangle(x - 1, y - 1, WIDTH + 2, rectangleHeight + 2, 0xFF000000);
                }
            }
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

    @Unique
    private float getMaxHealthFromBossBar(BossBar bossBar) {
        if (bossBar.getName().getString().equals("Ender Dragon")) {
            return 200;
        } else if (bossBar.getName().getString().equals("Wither")) {
            return 300;
        }
        return -1;
    }
}
