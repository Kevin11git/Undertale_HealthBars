package net.kevineleven.undertale_healthbars.mixin;


import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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


@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {


    @Shadow @Final private static Identifier[] BAR_BACKGROUND_SPRITES;

    @Shadow @Final private static int BAR_WIDTH;
    @Shadow @Final private static int BAR_HEIGHT;
    @Shadow @Final private static Identifier[] BAR_PROGRESS_SPRITES;
    @Unique
    Map<BossEvent, Float> oldHealths = new HashMap<>(); // for damage info

    @Inject(
            method = "extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;I[Lnet/minecraft/resources/Identifier;[Lnet/minecraft/resources/Identifier;)V",
            at = @At(value = "RETURN")
    )
    private void renderBossBar(GuiGraphicsExtractor graphics, int x, int y, BossEvent bossBar, int width, Identifier[] sprites, Identifier[] overlaySprites, CallbackInfo ci) {
        if (!(ModConfig.modEnabled)) {
            return;
        }

        if (isBossBarOfEntity(bossBar)) {
            int rectangleHeight = BAR_HEIGHT;

            if (!bossPreviousHealths.containsKey(bossBar)) {
                bossPreviousHealths.put(bossBar, bossBar.getProgress());
            }
            if (!oldHealths.containsKey(bossBar)) {
                oldHealths.put(bossBar, bossBar.getProgress() * getMaxHealthFromBossBar(bossBar));
            }

            float previousHealth = bossPreviousHealths.get(bossBar);

            if (Arrays.equals(sprites, BAR_BACKGROUND_SPRITES)){
                float oldHealth = oldHealths.get(bossBar);
                float newHealth = bossBar.getProgress() * getMaxHealthFromBossBar(bossBar);
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



                if (previousHealth != bossBar.getProgress()) {

                    if (previousHealth == 1 && bossBar.getProgress() <= 0.0f) { // if died in 1 hit
                        bossPreviousHealths.replace(bossBar, -10f); // negative so bar doesn't disappear instantly
                    }
                    else {
                        bossPreviousHealths.replace(bossBar, Mth.lerp(0.1f, previousHealth, bossBar.getProgress()));
                    }

                    if (Math.abs(previousHealth - bossBar.getProgress()) < 0.0000001) {
                        bossPreviousHealths.replace(bossBar, bossBar.getProgress());
                    }

                    previousHealth = bossPreviousHealths.get(bossBar);
                }

                if (ModConfig.showUndertaleBossbars) {
                    // gray background rect
                    graphics.fill(x, y, x + width, y + rectangleHeight, 0xFF404040);
                }

            } else if (Arrays.equals(sprites, BAR_PROGRESS_SPRITES)) {
                if (ModConfig.showUndertaleBossbars) {
                    // green health rect
                    graphics.fill(x, y, x + ((int) Math.ceil(Math.max(0.0f, previousHealth) * BAR_WIDTH)), y + rectangleHeight, 0xFF00D600);
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

                        int number_x = x + BAR_WIDTH + 5;
                        Identifier texture;
                        float scale = 0.5f;
                        graphics.pose().scale(scale, scale);
                        for (int index = 0; index < textDamage.length(); index++) {
                            char currentChar = textDamage.charAt(index);
                            if (currentChar == ',') {
                                currentChar = '.';
                            }
                            texture = Identifier.fromNamespaceAndPath(UndertaleHealthBarsClient.MOD_ID, "textures/ui/" + damage_or_heal + "_num_" + currentChar + ".png");
                            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, (int) (number_x * (1 / scale) + damageInfo.y_offset * 30), (int) ((y - Math.ceil(10 * scale)) * (1 / scale)), 0, 0, 30, 30, 30, 30);

                            number_x += (int) Math.ceil(31 * scale);
                        }
                        graphics.pose().scale(1 / scale, 1 / scale);
                    }
                }

                if (ModConfig.showUndertaleBossbars) {
                    // black outline
                    graphics.outline(x - 1, y - 1, BAR_WIDTH + 2, rectangleHeight + 2, 0xFF000000);
                }
            }
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

    @Unique
    private float getMaxHealthFromBossBar(BossEvent bossBar) {
        if (bossBar.getName().getString().equals("Ender Dragon")) {
            return 200;
        } else if (bossBar.getName().getString().equals("Wither")) {
            return 300;
        }
        return -1;
    }
}
