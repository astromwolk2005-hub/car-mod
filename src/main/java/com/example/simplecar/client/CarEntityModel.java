package com.example.simplecar.client;

import com.example.simplecar.entity.CarEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class CarEntityModel extends EntityModel<CarEntity> {
    private final ModelPart root;
    private final ModelPart frontLeftWheel;
    private final ModelPart frontRightWheel;
    private final ModelPart rearLeftWheel;
    private final ModelPart rearRightWheel;

    public CarEntityModel(ModelPart root) {
        this.root = root;
        this.frontLeftWheel = root.getChild("front_left_wheel");
        this.frontRightWheel = root.getChild("front_right_wheel");
        this.rearLeftWheel = root.getChild("rear_left_wheel");
        this.rearRightWheel = root.getChild("rear_right_wheel");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Main crimson body chassis
        root.addChild("chassis",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-11.0f, 15.0f, -20.0f, 22.0f, 6.0f, 40.0f)
                        .uv(0, 0).cuboid(-10.0f, 12.0f, -19.0f, 20.0f, 3.0f, 38.0f),
                ModelTransform.NONE);

        // Tinted cabin
        root.addChild("cabin",
                ModelPartBuilder.create()
                        .uv(0, 52).cuboid(-9.0f, 6.0f, -6.0f, 18.0f, 6.0f, 16.0f),
                ModelTransform.NONE);

        // Carbon-fiber bumpers & rear spoiler
        root.addChild("trim",
                ModelPartBuilder.create()
                        .uv(0, 84).cuboid(-11.5f, 17.0f, -21.5f, 23.0f, 4.0f, 3.0f)
                        .uv(0, 84).cuboid(-11.5f, 17.0f, 18.5f, 23.0f, 4.0f, 3.0f)
                        .uv(0, 84).cuboid(-10.5f, 8.5f, 16.0f, 21.0f, 1.5f, 4.0f)
                        .uv(0, 84).cuboid(-7.0f, 10.0f, 17.0f, 2.0f, 2.5f, 2.0f)
                        .uv(0, 84).cuboid(5.0f, 10.0f, 17.0f, 2.0f, 2.5f, 2.0f),
                ModelTransform.NONE);

        // Headlights & Taillights
        root.addChild("lights",
                ModelPartBuilder.create()
                        .uv(48, 104).cuboid(-9.5f, 14.0f, -20.5f, 4.0f, 2.5f, 1.0f)
                        .uv(48, 104).cuboid(5.5f, 14.0f, -20.5f, 4.0f, 2.5f, 1.0f)
                        .uv(76, 104).cuboid(-9.5f, 14.0f, 19.5f, 4.0f, 2.0f, 1.0f)
                        .uv(76, 104).cuboid(5.5f, 14.0f, 19.5f, 4.0f, 2.0f, 1.0f),
                ModelTransform.NONE);

        // 4 Wheels (radius 4, ground is y=24 so pivot is y=20)
        ModelPartBuilder wheelBuilder = ModelPartBuilder.create()
                .uv(0, 104).cuboid(-1.5f, -4.0f, -4.0f, 3.0f, 8.0f, 8.0f);

        root.addChild("front_right_wheel", wheelBuilder, ModelTransform.pivot(-11.5f, 20.0f, -12.0f));
        root.addChild("front_left_wheel", wheelBuilder, ModelTransform.pivot(11.5f, 20.0f, -12.0f));
        root.addChild("rear_right_wheel", wheelBuilder, ModelTransform.pivot(-11.5f, 20.0f, 12.0f));
        root.addChild("rear_left_wheel", wheelBuilder, ModelTransform.pivot(11.5f, 20.0f, 12.0f));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(CarEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float wheelRot = entity.getWheelRotation();
        this.frontLeftWheel.pitch = wheelRot;
        this.frontRightWheel.pitch = wheelRot;
        this.rearLeftWheel.pitch = wheelRot;
        this.rearRightWheel.pitch = wheelRot;

        float steer = entity.getSteeringAngle();
        this.frontLeftWheel.yaw = steer;
        this.frontRightWheel.yaw = steer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.root.render(matrices, vertices, light, overlay, color);
    }
}
