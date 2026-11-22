package com.example.simplecar.item;

import com.example.simplecar.SimpleCarMod;
import com.example.simplecar.entity.CarEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CarItem extends Item {
    public CarItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockPos pos = context.getBlockPos();
        Direction side = context.getSide();
        BlockPos spawnPos = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                ? pos
                : pos.offset(side);

        CarEntity car = new CarEntity(SimpleCarMod.CAR_ENTITY, world);
        Vec3d center = Vec3d.ofBottomCenter(spawnPos);
        car.refreshPositionAndAngles(center.x, center.y, center.z, context.getPlayerYaw(), 0.0f);

        if (!world.isSpaceEmpty(car)) {
            return ActionResult.FAIL;
        }

        world.spawnEntity(car);
        world.playSound(null, car.getBlockPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.7f, 1.3f);

        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (player == null || !player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return ActionResult.CONSUME;
    }
}
