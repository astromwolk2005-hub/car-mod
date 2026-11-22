package com.example.simplecar;

import com.example.simplecar.entity.CarEntity;
import com.example.simplecar.item.CarItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCarMod implements ModInitializer {
    public static final String MOD_ID = "simplecar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final EntityType<CarEntity> CAR_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "car"),
            EntityType.Builder.<CarEntity>create(CarEntity::new, SpawnGroup.MISC)
                    .dimensions(1.5f, 0.9f)
                    .maxTrackingRange(10)
                    .build()
    );

    public static final Item CAR_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "car"),
            new CarItem(new Item.Settings().maxCount(1))
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(CAR_ITEM);
        });
        LOGGER.info("Simple Car Mod initialized for Minecraft 1.21.1!");
    }
}
