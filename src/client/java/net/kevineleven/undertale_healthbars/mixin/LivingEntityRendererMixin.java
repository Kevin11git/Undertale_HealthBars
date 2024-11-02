package net.kevineleven.undertale_healthbars.mixin;


import com.mojang.blaze3d.systems.RenderSystem;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
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
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer {


    @Unique
    private static Map<LivingEntity, Float> previousHealths = new HashMap<>();

    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("RETURN"))
    public void render(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!shouldRenderForLivingEntity(livingEntity)) return;





        if (!previousHealths.containsKey(livingEntity)) {
            previousHealths.put(livingEntity, livingEntity.getHealth());
        }


        float previousHealth = previousHealths.get(livingEntity);

        if (previousHealth != livingEntity.getHealth()) {
            previousHealths.replace(livingEntity, lerp(previousHealth, livingEntity.getHealth(), 0.1f));
            if (Math.abs(previousHealth - livingEntity.getHealth()) < 0.0001) {
                previousHealths.replace(livingEntity, livingEntity.getHealth());
            }


            previousHealth = previousHealths.get(livingEntity);
            if (previousHealth <= 0.0f) {
                previousHealths.remove(livingEntity);
                //client.player.sendMessage(Text.of("removed " + livingEntity.getName().getString() + " size: " + previousHealths.size()));

            }
        }




        //client.player.sendMessage(Text.of("rendering " + livingEntity.getName().getString()));
        matrixStack.push();


        //client.player.sendMessage(Text.of(String.format("cam rot x: %f y: %f z: %f", this.dispatcher.getRotation().x, this.dispatcher.getRotation().y, this.dispatcher.getRotation().z)));


        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);

        matrixStack.translate(0, livingEntity.getHeight() + 0.5f, 0);
        if (this.hasLabel(livingEntity) && d <= 4096.0) {
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
        MinecraftClient client = MinecraftClient.getInstance();
        // TODO fix bar rendering behind some entities
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float width = 2.5f;
        float height = 0.25f;
        float healthPercent = previousHealth / livingEntity.getMaxHealth();

        drawQuad(model, buffer, 0,0,-0.001f,width+0.01f,height+0.01f,0f,0f,0f,1);
        drawQuad(model, buffer, 0,0,0,width,height,0.25f,0.25f,0.25f,1);
        float healthWidth = width * healthPercent;


        float healthOffset = -((healthWidth / 2) - (width / 2));
        drawQuad(model, buffer, healthOffset,0,0.001f,healthWidth,height,0,0.84f,0,1);




        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrixStack.pop();

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
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
        return !livingEntity.isInvisibleTo(UndertaleHealthBarsClient.client.player) && (livingEntity.isAlive() || previousHealths.containsKey(livingEntity));
    }

    @Override
    public Identifier getTexture(Entity entity) {
        return null;
    }

    @Unique
    float lerp(float a, float b, float f)
    {
        return a + ((b - a) * f);
    }
}
