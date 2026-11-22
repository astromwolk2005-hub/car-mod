package com.example.simplecar.client;

import com.example.simplecar.SimpleCarClient;
import com.example.simplecar.SimpleCarMod;
import com.example.simplecar.entity.CarEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class CarEntityRenderer extends EntityRenderer<CarEntity> {
    private static final Identifier TEXTURE = Identifier.of(SimpleCarMod.MOD_ID, "textures/entity/car.png");
    private final CarEntityModel model;

    public CarEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 1.1f;
        this.model = new CarEntityModel(ctx.getPart(SimpleCarClient.CAR_MODEL_LAYER));
    }

    @Override
    public void render(CarEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        float lerpedYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevYaw, entity.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - lerpedYaw));
        matrices.scale(-1.0f, -1.0f, 1.0f);
        matrices.translate(0.0f, -1.501f, 0.0f);

        this.model.setAngles(entity, 0.0f, 0.0f, entity.age + tickDelta, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.getTexture(entity)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, -1);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(CarEntity entity) {
        return TEXTURE;
    }
}
