package net.kevineleven.undertale_healthbars.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {

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
        int outline_size = 1;

        float healthPercent = player.getHealth() / player.getMaxHealth();
        int barColor = getHpBarColor(type);
        int outline_color = 0xFF000000;

        // black outline, empty rect, and filled rect
        graphics.fill(x, y, x + width, y + height, outline_color);
        graphics.fill(x + outline_size, y + outline_size, x + width - outline_size, y + height - outline_size, 0xFFFF0000);
        graphics.fill(x + outline_size, y + outline_size, x + (int)((width - (outline_size * 2)) * healthPercent) + outline_size, y + height - outline_size, barColor);

        // Second inner outline for hardcore mode
        if (isHardcore) {
            graphics.outline(x + outline_size, y + outline_size, width - (outline_size * 2), height - (outline_size * 2), 0xFF990000);
        }

        drawHpNumber(graphics, x - 1, y - 2, player);

        ci.cancel();
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
            case POISIONED -> 0xFF37946E;
            case WITHERED -> 0xFF765200;
            case FROZEN -> 0xFF00d0ff           ;
            default -> 0xFFFBF236; // NORMAL
        };
    }
}
