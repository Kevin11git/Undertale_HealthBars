package net.kevineleven.undertale_healthbars.mixin;

import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.damageInfos;
import static net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient.previousHealths;

// Thank you AdyTech99 for your Health Indicators mod!
// It helped me update this mod to 1.21.4 and later versions
// https://modrinth.com/mod/health-indicators
// I only used this class for help im pretty sure
// https://github.com/AdyTech99/HealthIndicators/blob/main/common/src/main/java/io/github/adytech99/healthindicators/mixin/EntityRendererMixin.java

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M> {

    @Unique private static final java.util.WeakHashMap<LivingEntityRenderState, LivingEntity> ENTITY_MAP = new java.util.WeakHashMap<>();

    protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }


    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci){
        // Store the entity in a WeakHashMap keyed by the render state
        // This prevents the health sharing bug where entities of the same type show the same health
        // Using WeakHashMap ensures render states are garbage collected when no longer needed
        ENTITY_MAP.put(livingEntityRenderState, livingEntity);
    }
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("RETURN"))
    public void render(S livingEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        // Retrieve the entity from the map
        LivingEntity livingEntity = ENTITY_MAP.get(livingEntityRenderState);

        if (livingEntity == null) {
            return;
        }
        if (!(shouldRenderForLivingEntity(livingEntity))) {
            return;
        }

        if (!previousHealths.containsKey(livingEntity)) {
            previousHealths.put(livingEntity, livingEntity.getHealth());
        }


        float previousHealth = previousHealths.get(livingEntity);

        if (previousHealth != livingEntity.getHealth()) {

            if (previousHealth == livingEntity.getMaxHealth() && livingEntity.getHealth() <= 0.0f) { // if died in 1 hit
                previousHealths.replace(livingEntity, -10f); // negative so bar doesn't disappear instantly
            }
            else {
                previousHealths.replace(livingEntity, lerp(previousHealth, livingEntity.getHealth(), 0.2f));
            }

            if (Math.abs(previousHealth - livingEntity.getHealth()) < 0.0001) {
                previousHealths.replace(livingEntity, livingEntity.getHealth());
            }

            previousHealth = previousHealths.get(livingEntity);
            if (previousHealth == 0.0f) {
                previousHealths.remove(livingEntity);
            }
        }


        if (!(ModConfig.modEnabled)) {
            return;
        }


        if (damageInfos.containsKey(livingEntity) || ModConfig.alwaysShowHealthbar) {

            MinecraftClient client = MinecraftClient.getInstance();

            matrixStack.push();

            float x = 0.0f;
            float y = 0.0f;
            float z = 0.0f;

            double d = livingEntity.distanceTo(client.getCameraEntity());
            matrixStack.translate(x, y + livingEntity.getHeight() + 0.5f + ModConfig.healthbarOffset, z);
            if ((livingEntity.hasCustomName() && d <= 4096.0) || (livingEntity instanceof PlayerEntity)) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);

                if (livingEntity instanceof PlayerEntity playerEntity) {
                    if (d < 100.0 && playerEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                    }
                }
            }


            assert this.dispatcher.camera != null;
            matrixStack.multiply(this.dispatcher.camera.getRotation());

            matrixStack.scale(-1, 1, 1);

            if (ModConfig.showHealthbar && (damageInfos.containsKey(livingEntity) || ModConfig.alwaysShowHealthbar)) {

                float width = 2.5f;
                float height = 0.25f;
                float healthPercent = Math.max(0.0f, previousHealth) / livingEntity.getMaxHealth();

                drawQuad(matrixStack, queue, 0, 0, -0.001f, width + 0.01f, height + 0.01f, 0f, 0f, 0f, 1.0f);
                drawQuad(matrixStack, queue, 0, 0, 0, width, height, 0.25f, 0.25f, 0.25f, 1);
                float healthWidth = width * healthPercent;


                float healthOffset = -((healthWidth / 2) - (width / 2));
                drawQuad(matrixStack, queue, healthOffset, 0, 0.001f, healthWidth, height, 0, 0.84f, 0, 1.0f);


                // Might add health number/percent text later
//                TextRenderer textRenderer = client.textRenderer;
//                matrixStack.scale(0.1f,0.1f,0.1f);
//                matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180));
//                String text = String.format("%.1f", livingEntity.getHealth());
//
//                matrixStack.translate(-((float) (textRenderer.getWidth(text)) / 2), 1 * -10, 0);
//
//
//
//                textRenderer.draw(text, 0f, 0, 0xFF0000, false, model, client.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0x000000, 15);
            }

            if (damageInfos.containsKey(livingEntity)) {

                DamageInfo damageInfo = damageInfos.get(livingEntity);
                float damage = damageInfo.damage;
                String damage_or_heal = "damage";
                if (damage < 0.0) {
                    damage_or_heal = "heal";
                    damage = Math.abs(damage);
                }

                if (
                        (damage_or_heal == "damage" && ModConfig.showDamageNumbers) ||
                                (damage_or_heal == "heal" && ModConfig.showHealNumbers)
                ) {
                    String textDamage;
                    if (Math.floor(damage) == damage) {
                        textDamage = String.format("%.0f", damage);
                    } else {
                        textDamage = String.format("%.2f", damage);
                    }

                    matrixStack.scale(0.5f, 0.5f, 0.5f);

                    x = ((textDamage.length() - 1f) * 1.1f) / 2f;
                    Identifier texture;
                    for (int index = 0; index < textDamage.length(); index++) {
                        char currentChar = textDamage.charAt(index);
                        if (currentChar == ',') {
                            currentChar = '.';
                        }
                        texture = Identifier.of(UndertaleHealthBarsClient.MOD_ID, "textures/ui/" + damage_or_heal + "_num_" + currentChar + ".png");
                        drawDamageNumber(matrixStack, queue, texture, x, 1f + damageInfo.y_offset, 0, 1, 1, 1, 1);

                        x -= 1.1f;
                    }
                }

            }

            matrixStack.pop();
        }

    }


    @Unique
    private void drawQuad(MatrixStack matrixStack, OrderedRenderCommandQueue queue,
                          float x,
                          float y,
                          float z,
                          float width,
                          float height,
                          float r,
                          float g,
                          float b,
                          float a
    ) {
        // could also prob use RenderLayer.getLightning(); if i dont want to specify light
        queue.submitCustom(matrixStack, RenderLayer.getTextBackground(), (matricesEntry, buffer) -> {
            Matrix4f model = matricesEntry.getPositionMatrix();

            // Bottom Left <v
            buffer.vertex(model, x + (width / 2), (y - height) + (height / 2), z).color(r, g, b, a).light(15728880);
            // Bottom Right >v
            buffer.vertex(model, (x - width) + (width / 2), (y - height) + (height / 2), z).color(r, g, b, a).light(15728880);
            // Top Right >^
            buffer.vertex(model, (x - width) + (width / 2), y + (height / 2), z).color(r, g, b, a).light(15728880);
            // Top Left <^
            buffer.vertex(model, x + (width / 2), y + (height / 2), z).color(r, g, b, a).light(15728880);
        });
    }
    @Unique
    private void drawDamageNumber(MatrixStack matrixStack, OrderedRenderCommandQueue queue, Identifier texture,
                                  float x,
                                  float y,
                                  float z,
                                  float width,
                                  float height,
                                  float textureWidth,
                                  float textureHeight
    ) {

        queue.submitCustom(matrixStack, RenderLayer.getText(texture), (matricesEntry, buffer) -> {
            Matrix4f model = matricesEntry.getPositionMatrix();

            // Bottom Left <v
            buffer.vertex(model, x + (width / 2), (y - height) + (height / 2), z).texture(0, textureHeight).light(15728880).color(1f, 1f, 1f, 1f);
            // Bottom Right >v
            buffer.vertex(model, (x - width) + (width / 2), (y - height) + (height / 2), z).texture(textureWidth, textureHeight).light(15728880).color(1f, 1f, 1f, 1f);
            // Top Right >^
            buffer.vertex(model, (x - width) + (width / 2), y + (height / 2), z).texture(textureWidth, 0).light(15728880).color(1f, 1f, 1f, 1f);
            // Top Left <^
            buffer.vertex(model, x + (width / 2), y + (height / 2), z).texture(0, 0).light(15728880).color(1f, 1f, 1f, 1f);
        });
    }


    @Unique
    private boolean shouldRenderForLivingEntity(LivingEntity livingEntity) {
        boolean isCurrentPlayer = false;

        if (livingEntity instanceof PlayerEntity playerEntity && !ModConfig.renderForYourself) {
            if (UndertaleHealthBarsClient.client.player.equals(playerEntity)) {
                isCurrentPlayer = true;
            }
        }



        return !livingEntity.isInvisibleTo(UndertaleHealthBarsClient.client.player) &&
                (livingEntity.isAlive() || previousHealths.containsKey(livingEntity)) &&
                !Objects.equals(livingEntity.getType().getName().getString(), "Armor Stand") &&
                !isCurrentPlayer
                ;
    }

    @Unique
    float lerp(float a, float b, float f)
    {
        return a + ((b - a) * f);
    }
}
