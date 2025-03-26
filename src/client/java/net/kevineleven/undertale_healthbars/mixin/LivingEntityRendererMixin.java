package net.kevineleven.undertale_healthbars.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.config.ModConfig;
import net.kevineleven.undertale_healthbars.util.DamageInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
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

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin extends EntityRenderer {

    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("RETURN"))
    public void render(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {


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
                previousHealths.replace(livingEntity, lerp(previousHealth, livingEntity.getHealth(), 0.1f));
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

            double d = livingEntity.distanceTo(client.cameraEntity);
            matrixStack.translate(x, y + livingEntity.getHeight() + 0.5f + ModConfig.healthbarOffset, z);
            if ((livingEntity.hasCustomName() && d <= 4096.0) || (livingEntity instanceof PlayerEntity)) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);

                if (livingEntity instanceof PlayerEntity playerEntity) {

                    if (d < 100.0 && playerEntity.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                    }
                }
            }


            matrixStack.multiply(this.dispatcher.getRotation());

            matrixStack.scale(-1, 1, 1);

            Matrix4f model = matrixStack.peek().getPositionMatrix();
            RenderSystem.enableDepthTest();

            BufferBuilder buffer;

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            if (ModConfig.showHealthbar && (damageInfos.containsKey(livingEntity) || ModConfig.alwaysShowHealthbar)) {
                buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

                float width = 2.5f;
                float height = 0.25f;
                float healthPercent = Math.max(0.0f, previousHealth) / livingEntity.getMaxHealth();

                drawQuad(model, buffer, 0, 0, -0.001f, width + 0.01f, height + 0.01f, 0f, 0f, 0f, 1);
                drawQuad(model, buffer, 0, 0, 0, width, height, 0.25f, 0.25f, 0.25f, 1);
                float healthWidth = width * healthPercent;


                float healthOffset = -((healthWidth / 2) - (width / 2));
                drawQuad(model, buffer, healthOffset, 0, 0.001f, healthWidth, height, 0, 0.84f, 0, 1);


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

                BufferRenderer.drawWithGlobalProgram(buffer.end());
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
                        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

                        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                        char currentChar = textDamage.charAt(index);
                        if (currentChar == ',') {
                            currentChar = '.';
                        }
                        texture = Identifier.of(UndertaleHealthBarsClient.MOD_ID, "textures/ui/" + damage_or_heal + "_num_" + currentChar + ".png");

                        RenderSystem.setShaderTexture(0, texture);
                        drawDamageNumber(model, buffer, x, 1f + damageInfo.y_offset, 0, 1, 1, 1, 1);
                        BufferRenderer.drawWithGlobalProgram(buffer.end());

                        x -= 1.1f;
                    }
                }

            }

            matrixStack.pop();
            RenderSystem.disableDepthTest();
        }

    }


    @Unique
    private void drawQuad(Matrix4f model, BufferBuilder buffer,
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
        // Bottom Left <v
        buffer.vertex(model, x + (width / 2), (y - height) + (height / 2), z).color(r, g, b, a);
        // Bottom Right >v
        buffer.vertex(model, (x - width) + (width / 2), (y - height) + (height / 2), z).color(r, g, b, a);
        // Top Right >^
        buffer.vertex(model, (x - width) + (width / 2), y + (height / 2), z).color(r, g, b, a);
        // Top Left <^
        buffer.vertex(model, x + (width / 2), y + (height / 2), z).color(r, g, b, a);
    }
    @Unique
    private void drawDamageNumber(Matrix4f model, BufferBuilder buffer,
                                  float x,
                                  float y,
                                  float z,
                                  float width,
                                  float height,
                                  float textureWidth,
                                  float textureHeight
    ) {
        // Bottom Left <v
        buffer.vertex(model, x + (width / 2), (y - height) + (height / 2), z).texture(0, textureHeight);
        // Bottom Right >v
        buffer.vertex(model, (x - width) + (width / 2), (y - height) + (height / 2), z).texture(textureWidth, textureHeight);
        // Top Right >^
        buffer.vertex(model, (x - width) + (width / 2), y + (height / 2), z).texture(textureWidth, 0);
        // Top Left <^
        buffer.vertex(model, x  + (width / 2), y + (height / 2), z).texture(0, 0);

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

    @Override
    public Identifier getTexture(Entity entity) {
        return null;
    }
}
