package net.kevineleven.undertale_healthbars.mixin;

import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.resources.Identifier;
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
        implements RenderLayerParent<S, M> {

    @Unique private static final java.util.WeakHashMap<LivingEntityRenderState, LivingEntity> ENTITY_MAP = new java.util.WeakHashMap<>();

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }


    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci){
        // Store the entity in a WeakHashMap keyed by the render state
        // This prevents the health sharing bug where entities of the same type show the same health
        // Using WeakHashMap ensures render states are garbage collected when no longer needed
        ENTITY_MAP.put(livingEntityRenderState, livingEntity);
    }
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("RETURN"))
    public void render(S livingEntityRenderState, PoseStack matrixStack, SubmitNodeCollector queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
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

        // Check if entity is within distance to show using the max distance config option
        double distanceToCamera = livingEntity.distanceTo(UndertaleHealthBarsClient.client.getCameraEntity());
        if (ModConfig.maxDistance < ModConfig.MAXIMUM_MAX_DISTANCE) { // don't check if distance is infinite
            if (distanceToCamera > ModConfig.maxDistance) {
                // Entity is outside max distance, don't show Healthbar and damage numbers etc
                return;
            }
        }


        if (damageInfos.containsKey(livingEntity) || ModConfig.alwaysShowHealthbar) {

            Minecraft client = Minecraft.getInstance();

            matrixStack.pushPose();

            float x = 0.0f;
            float y = 0.0f;
            float z = 0.0f;

            double d = livingEntity.distanceTo(client.getCameraEntity());
            matrixStack.translate(x, y + livingEntity.getBbHeight() + 0.5f + ModConfig.healthbarOffset, z);
            if ((livingEntity.hasCustomName() && d <= 4096.0) || (livingEntity instanceof Player)) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);

                if (livingEntity instanceof Player playerEntity) {
                    if (d < 100.0 && playerEntity.level().getScoreboard().getDisplayObjective(DisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                    }
                }
            }


            assert this.entityRenderDispatcher.camera != null;
            matrixStack.mulPose(this.entityRenderDispatcher.camera.rotation());

            matrixStack.scale(-1, 1, 1);

            if (ModConfig.showHealthbar && (damageInfos.containsKey(livingEntity) || ModConfig.alwaysShowHealthbar)) {

                float width = 2.5f;
                float height = 0.25f;
                float healthPercent = Math.max(0.0f, previousHealth) / livingEntity.getMaxHealth();
                healthPercent = Math.min(healthPercent, 1.0f); // make sure health bar doesn't go above 100%

                drawQuad(matrixStack, queue, 0, 0, -0.01f, width + 0.01f, height + 0.01f, 0f, 0f, 0f, 1.0f);
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
                        texture = Identifier.fromNamespaceAndPath(UndertaleHealthBarsClient.MOD_ID, "textures/ui/" + damage_or_heal + "_num_" + currentChar + ".png");
                        drawDamageNumber(matrixStack, queue, texture, x, 1f + damageInfo.y_offset, 0, 1, 1, 1, 1);

                        x -= 1.1f;
                    }
                }

            }

            matrixStack.popPose();
        }

    }


    @Unique
    private void drawQuad(PoseStack matrixStack, SubmitNodeCollector queue,
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
        // could also prob use RenderLayers.lightning(); if i dont want to specify light
        queue.submitCustomGeometry(matrixStack, RenderTypes.textBackground(), (matricesEntry, buffer) -> {
            Matrix4f model = matricesEntry.pose();

            // Bottom Left <v
            buffer.addVertex(model, x + (width / 2), (y - height) + (height / 2), z).setColor(r, g, b, a).setLight(15728880);
            // Bottom Right >v
            buffer.addVertex(model, (x - width) + (width / 2), (y - height) + (height / 2), z).setColor(r, g, b, a).setLight(15728880);
            // Top Right >^
            buffer.addVertex(model, (x - width) + (width / 2), y + (height / 2), z).setColor(r, g, b, a).setLight(15728880);
            // Top Left <^
            buffer.addVertex(model, x + (width / 2), y + (height / 2), z).setColor(r, g, b, a).setLight(15728880);
        });
    }
    @Unique
    private void drawDamageNumber(PoseStack matrixStack, SubmitNodeCollector queue, Identifier texture,
                                  float x,
                                  float y,
                                  float z,
                                  float width,
                                  float height,
                                  float textureWidth,
                                  float textureHeight
    ) {

        queue.submitCustomGeometry(matrixStack, RenderTypes.text(texture), (matricesEntry, buffer) -> {
            Matrix4f model = matricesEntry.pose();

            // Bottom Left <v
            buffer.addVertex(model, x + (width / 2), (y - height) + (height / 2), z).setUv(0, textureHeight).setLight(15728880).setColor(1f, 1f, 1f, 1f);
            // Bottom Right >v
            buffer.addVertex(model, (x - width) + (width / 2), (y - height) + (height / 2), z).setUv(textureWidth, textureHeight).setLight(15728880).setColor(1f, 1f, 1f, 1f);
            // Top Right >^
            buffer.addVertex(model, (x - width) + (width / 2), y + (height / 2), z).setUv(textureWidth, 0).setLight(15728880).setColor(1f, 1f, 1f, 1f);
            // Top Left <^
            buffer.addVertex(model, x + (width / 2), y + (height / 2), z).setUv(0, 0).setLight(15728880).setColor(1f, 1f, 1f, 1f);
        });
    }


    @Unique
    private boolean shouldRenderForLivingEntity(LivingEntity livingEntity) {
        boolean isCurrentPlayer = false;

        if (livingEntity instanceof Player playerEntity && !ModConfig.renderForYourself) {
            if (UndertaleHealthBarsClient.client.player.equals(playerEntity)) {
                isCurrentPlayer = true;
            }
        }



        return !livingEntity.isInvisibleTo(UndertaleHealthBarsClient.client.player) &&
                (livingEntity.isAlive() || previousHealths.containsKey(livingEntity)) &&
                !Objects.equals(livingEntity.getType().getDescription().getString(), "Armor Stand") &&
                !isCurrentPlayer
                ;
    }

    @Unique
    float lerp(float a, float b, float f)
    {
        return a + ((b - a) * f);
    }
}
