package com.example.simplecar;

import com.example.simplecar.client.CarEntityModel;
import com.example.simplecar.client.CarEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class SimpleCarClient implements ClientModInitializer {
    public static final EntityModelLayer CAR_MODEL_LAYER =
            new EntityModelLayer(Identifier.of(SimpleCarMod.MOD_ID, "car"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(CAR_MODEL_LAYER, CarEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(SimpleCarMod.CAR_ENTITY, CarEntityRenderer::new);
    }
}
