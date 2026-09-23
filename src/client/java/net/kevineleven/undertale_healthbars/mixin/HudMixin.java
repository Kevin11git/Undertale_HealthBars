package net.kevineleven.undertale_healthbars.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Hud.class)
public class HudMixin {

    @Unique
    final int NORMAL_HP_BAR_COLOR = 0xFFFBF236;

    @Inject(
            method = "extractHearts",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(D)I", ordinal = 0),
            cancellable = true)
    private void extractHearts(GuiGraphicsExtractor graphics, Player player, int x, int y, int healthRowHeight, int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, CallbackInfo ci, @Local(name = "type") Hud.HeartType type, @Local(name = "isHardcore") boolean isHardcore) {
        if (!(ModConfig.modEnabled.get())) {
            return;
        }
        int width = 9 * 9;
        int height = 9;
        int outlineSize = 1;

        float healthPercent = player.getHealth() / player.getMaxHealth();
        int barColor = getHpBarColor(type);

        float healthEst;
        float healthEstPercent;

        // DRAW THE MAIN HP BAR HEALTH BAR
        drawBar(graphics, x, y, width, height, healthPercent, barColor, outlineSize, true, 0);

        // Damage estimation (like how karma shows in sans fight)
        if (
            player.hasEffect(MobEffects.POISON) ||
            player.hasEffect(MobEffects.WITHER)
        ) {
            float damageEstimation = damageEstimation(player);
            healthEst = Math.max(player.getHealth() - damageEstimation, 0f);
            if (healthEst <= 0 && !player.hasEffect(MobEffects.WITHER)) { // See how low health can be with poisSON
                healthEst = (float) (player.getHealth() - Math.floor(player.getHealth()));
                if (healthEst <= 0) // If health was whole number, poison damage will be to 1
                {
                    healthEst = 1;
                }
            }
            healthEstPercent = healthEst / player.getMaxHealth();
            drawBarScaled(-1, graphics, x, y, width, height, healthEstPercent, NORMAL_HP_BAR_COLOR, 0, false, 0);
        }
        // Heal estimation from regeneration
        if (player.hasEffect(MobEffects.REGENERATION)) {
            float healEst = healEstimation(player);
            healthEst = Math.min(player.getHealth() + healEst, player.getMaxHealth());
            if (healthEst > player.getHealth()) {
                healthEstPercent = healthEst / player.getMaxHealth();
                int healEstColor = 0xFF5e5b17;
                drawBarScaled(-1, graphics, x, y, width, height, healthEstPercent, healEstColor, 0, false, healthPercent);
            }
        }

        // Second inner outline for hardcore mode
        if (isHardcore) {
            graphics.outline(x + outlineSize, y + outlineSize, width - (outlineSize * 2), height - (outlineSize * 2), 0xFF990000);
        }

        drawHpNumber(graphics, x - 1, y - 2, player);

        ci.cancel();
    }

    @Unique
    private static void drawBarScaled(int scaleOffset, GuiGraphicsExtractor graphics, int x, int y, int width, int height, float percent, int color, int outline_size, boolean hasBg, float starting_percent) {
        drawBar(graphics, x - scaleOffset, y - scaleOffset, width + (scaleOffset * 2), height  + (scaleOffset * 2), percent, color, outline_size, hasBg, starting_percent);
    }
    @Unique
    private static void drawBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float percent, int color, int outline_size, boolean hasBg, float starting_percent) {
        // black outline, red empty rect, and filled rect
        if (outline_size > 0) {
            graphics.fill(x, y, x + width, y + height, 0xFF000000);
        }
        if (hasBg) {
            graphics.fill(x + outline_size, y + outline_size, x + width - outline_size, y + height - outline_size, 0xFFFF0000);
        }
        graphics.fill(x + (int)((width - (outline_size * 2)) * starting_percent) + outline_size, y + outline_size, x + (int)((width - (outline_size * 2)) * percent) + outline_size, y + height - outline_size, color);
    }

    @Unique
    private void drawHpNumber(GuiGraphicsExtractor graphics, int x, int y, LivingEntity livingEntity) {
        Font client_font = UndertaleHealthBarsClient.client.font;
        FontDescription font = new FontDescription.Resource(Identifier.fromNamespaceAndPath(UndertaleHealthBarsClient.MOD_ID, "marsneedscunnilingus"));
        float health_percent = livingEntity.getHealth() / livingEntity.getMaxHealth();

        String text = "Error: " + ModConfig.healthNumberDisplayType.get();
        switch (ModConfig.healthNumberDisplayType.get()) {
            case ModConfig.HealthNumberDisplayType.HEALTH_AND_MAX_HEALTH ->
                    text = formatFloat(livingEntity.getHealth()) + "/" + formatFloat(livingEntity.getMaxHealth());
            case ModConfig.HealthNumberDisplayType.HEALTH_ONLY ->
                    text = formatFloat(livingEntity.getHealth());
            case ModConfig.HealthNumberDisplayType.PERCENTAGE ->
                    text = formatFloat(health_percent * 100) + "%";
        }
        MutableComponent component = Component.literal(text).withStyle(Style.EMPTY.withFont(font));

        int font_color = CommonColors.WHITE;
        if (ModConfig.coloredHealthNumber.get()) {
            if (health_percent <= 0.5f) {
                font_color = CommonColors.YELLOW;
            }
            if (health_percent <= 0.25f) {
                font_color = CommonColors.SOFT_RED;
            }
        }

        x -= client_font.width(component);
        int shadow_color = CommonColors.BLACK;
        graphics.text(client_font, component.getVisualOrderText(), x + 1, y + 1, shadow_color, false);
        graphics.text(client_font, component.getVisualOrderText(), x, y, font_color, false);
    }
    @Unique
    private String formatFloat(float number) {
        String output = String.format("%.2f", number);
        if (output.endsWith(".00")) {
            output = String.format("%.0f", number);
        }
        return output;
    }

    @Unique
    private int getHpBarColor(Hud.HeartType type) {
         return switch (type) {
            case POISIONED -> 0xFFFF00FF;
            case WITHERED -> 0xFF202020;
            case FROZEN -> 0xFF00d0ff;
            default -> NORMAL_HP_BAR_COLOR; // Yellow
        };
    }

    @Unique
    private float damageEstimation(Player player) {
        float dmg; // the output damage

        double psnTime = getEffectDuration(player, MobEffects.POISON);
        double psnLvl  = getEffectLevel(player, MobEffects.POISON) % 32; // leaves player 's HP at 1
        double wthTime = getEffectDuration(player, MobEffects.WITHER);
        double wthLvl  = getEffectLevel(player, MobEffects.WITHER) % 32; // can kill the player
        double resTime = getEffectDuration(player, MobEffects.RESISTANCE);
        double resLvl  = getEffectLevel(player, MobEffects.RESISTANCE) % 32;


        if (psnLvl == 0)
            psnTime = Math.floor(psnTime / 25);
        else if (psnLvl==1)
            psnTime = Math.floor(psnTime / 12);
        else if (psnLvl==2)
            psnTime = Math.floor(psnTime / 12); // technically it should be divided by 6 but iframes exist
        else if (psnLvl==3)
            psnTime = Math.floor(psnTime / 12); // should be 3 but ditto
        else
            psnTime = Math.floor(psnTime / 10); // should be 1 but ditto

        if (wthLvl == 0)
            wthTime = Math.floor(wthTime / 40);
        else if (wthLvl==1)
            wthTime = Math.floor(wthTime / 20);
        else
            wthTime = Math.floor(wthTime / 10); // lv2 -> 10 t, lv3 -> 5 t, lv4 -> 2 t, lv5 +->1 t, but iframes

        dmg = (float) (psnTime + wthTime); // assuming player 's health is enough to deplete

        if (resTime > 0)
            return (float) Math.max(0, dmg * (1 - 0.2 * (resLvl + 1)));
        else
            return dmg;
    }

    @Unique
    private float healEstimation(Player player) {
        double heals = getEffectDuration(player, MobEffects.REGENERATION);
        double eqLvl = getEffectLevel(player, MobEffects.REGENERATION) % 32;
        if (eqLvl==0)
            heals = Math.floor(heals/50);
        else if (eqLvl==1)
            heals = Math.floor(heals/25);
        else if (eqLvl==2)
            heals = Math.floor(heals/12);
        else if (eqLvl==3)
            heals = Math.floor(heals/6);
        else if (eqLvl==4)
            heals = Math.floor(heals/3);

        return (float) heals;
    }

    // IN TICKS! (1 second = 20 ticks)
    @Unique
    private int getEffectDuration(LivingEntity livingEntity, Holder<MobEffect> effect) {
        if (!livingEntity.hasEffect(effect)) return 0;

        return Objects.requireNonNull(livingEntity.getEffect(effect)).getDuration();
    }
    @Unique
    private int getEffectLevel(LivingEntity livingEntity, Holder<MobEffect> effect) {
        if (!livingEntity.hasEffect(effect)) return -1;

        return Objects.requireNonNull(livingEntity.getEffect(effect)).getAmplifier();
    }
}
