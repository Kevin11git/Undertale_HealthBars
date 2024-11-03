package net.kevineleven.undertale_healthbars.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;


// todo fix bar rendering behind some stuff like items or particles
public class HealthBarRenderer implements WorldRenderEvents.AfterEntities {

    @Unique
    private static Map<LivingEntity, Float> previousHealths = new HashMap<>();

    @Override
    public void afterEntities(WorldRenderContext context) {

        for (Entity entity : context.world().getEntities()) {
            if (!(entity instanceof LivingEntity)) {
                    continue;
            }

            LivingEntity livingEntity;
            try {
                livingEntity = ((LivingEntity) entity);
            } catch (Exception e) {
                continue;
            }

            if (!(shouldRenderForLivingEntity(livingEntity))) {
                continue;
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



            MinecraftClient client = MinecraftClient.getInstance();
            MatrixStack matrixStack = context.matrixStack();

            matrixStack.push();

            float x = (float) (livingEntity.getX() - context.camera().getPos().x);
            float y = (float) (livingEntity.getY() - context.camera().getPos().y);
            float z = (float) (livingEntity.getZ() - context.camera().getPos().z);

            double d = livingEntity.distanceTo(client.cameraEntity);
            matrixStack.translate(x, y + livingEntity.getHeight() + 0.5f, z);
            if ((livingEntity.hasCustomName() && d <= 4096.0) || (livingEntity instanceof PlayerEntity)) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);

                if (livingEntity instanceof PlayerEntity playerEntity) {

                    if (d < 100.0 && playerEntity.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                    }
                }
            }


            matrixStack.multiply(context.camera().getRotation());

            matrixStack.scale(-1, 1, 1);

            Matrix4f model = matrixStack.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();


            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            float width = 2.5f;
            float height = 0.25f;
            float healthPercent = Math.max(0.0f, previousHealth) / livingEntity.getMaxHealth();

            drawQuad(model, buffer, 0, 0, -0.001f, width + 0.01f, height + 0.01f, 0f, 0f, 0f, 1);
            drawQuad(model, buffer, 0, 0, 0, width, height, 0.25f, 0.25f, 0.25f, 1);
            float healthWidth = width * healthPercent;


            float healthOffset = -((healthWidth / 2) - (width / 2));
            drawQuad(model, buffer, healthOffset, 0, 0.001f, healthWidth, height, 0, 0.84f, 0, 1);


            BufferRenderer.drawWithGlobalProgram(buffer.end());

            matrixStack.pop();

            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.depthFunc(515);
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
        buffer.vertex(model, x + (width / 2), (y - height) + (height / 2), z).color(r,g,b,a);
        buffer.vertex(model, (x - width) + (width / 2), (y - height) + (height / 2), z).color(r,g,b,a);
        buffer.vertex(model, (x - width) + (width / 2), y + (height / 2), z).color(r,g,b,a);
        buffer.vertex(model, x  + (width / 2), y + (height / 2), z).color(r,g,b,a);

    }


    @Unique
    private boolean shouldRenderForLivingEntity(LivingEntity livingEntity) {
        boolean isCurrentPlayer = false;
        if (livingEntity instanceof PlayerEntity playerEntity) {
            if (UndertaleHealthBarsClient.client.player.equals(playerEntity)) {
                isCurrentPlayer = true;
            }
        }



        return !livingEntity.isInvisibleTo(UndertaleHealthBarsClient.client.player) &&
                (livingEntity.isAlive() || previousHealths.containsKey(livingEntity)) &&
                !isCurrentPlayer
                ;
    }

    @Unique
    float lerp(float a, float b, float f)
    {
        return a + ((b - a) * f);
    }

}
