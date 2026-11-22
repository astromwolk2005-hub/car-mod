package com.example.simplecar.entity;

import com.example.simplecar.SimpleCarMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CarEntity extends Entity {
    private static final double MAX_FORWARD_SPEED = 0.85;
    private static final double MAX_REVERSE_SPEED = 0.35;
    private static final double ACCELERATION = 0.045;
    private static final double BRAKE_DECELERATION = 0.065;
    private static final double FRICTION = 0.92;
    private static final float TURN_SPEED = 4.2f;

    private double currentSpeed = 0.0;
    private float wheelRotation = 0.0f;
    private float steeringAngle = 0.0f;
    private float damageAccumulated = 0.0f;
    private int hurtCooldown = 0;

    public CarEntity(EntityType<? extends CarEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public float getStepHeight() {
        return 1.0f;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        }
        if (!this.getWorld().isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return new Vec3d(0.0, 0.32 * scaleFactor, -0.1 * scaleFactor)
                .rotateY(-this.getYaw() * ((float) Math.PI / 180.0f));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hurtCooldown > 0) {
            this.hurtCooldown--;
        } else if (this.damageAccumulated > 0.0f) {
            this.damageAccumulated = Math.max(0.0f, this.damageAccumulated - 0.1f);
        }

        if (this.isLogicalSideForUpdatingMovement()) {
            if (this.getWorld().isClient) {
                this.updateClientDriving();
            } else {
                this.applyIdleGravity();
            }
            this.move(MovementType.SELF, this.getVelocity());
        } else {
            this.setVelocity(Vec3d.ZERO);
        }

        // Update wheel visuals on client
        if (this.getWorld().isClient) {
            double horizSpeed = Math.hypot(this.getX() - this.prevX, this.getZ() - this.prevZ);
            if (horizSpeed > 0.002) {
                float dir = this.currentSpeed >= 0.0 ? 1.0f : -1.0f;
                this.wheelRotation += (float) (horizSpeed * 2.4 * dir);

                // Exhaust smoke particle at rear pipe
                if (this.age % 2 == 0) {
                    float rad = this.getYaw() * ((float) Math.PI / 180.0f);
                    double backX = MathHelper.sin(rad) * 1.25;
                    double backZ = -MathHelper.cos(rad) * 1.25;
                    this.getWorld().addParticle(
                            ParticleTypes.SMOKE,
                            this.getX() + backX,
                            this.getY() + 0.25,
                            this.getZ() + backZ,
                            backX * 0.05,
                            0.02,
                            backZ * 0.05
                    );
                }
            }
        }
    }

    private void applyIdleGravity() {
        double vy = this.isOnGround() ? -0.04 : Math.max(this.getVelocity().y - 0.08, -2.0);
        this.setVelocity(this.getVelocity().x * 0.85, vy, this.getVelocity().z * 0.85);
    }

    private void updateClientDriving() {
        LivingEntity controller = this.getControllingPassenger();
        float targetSteer = 0.0f;

        if (controller instanceof PlayerEntity driver) {
            float forward = driver.forwardSpeed;   // > 0: W, < 0: S
            float sideways = driver.sidewaysSpeed; // > 0: A (left), < 0: D (right)

            if (forward > 0.01f) {
                if (this.currentSpeed < 0.0) {
                    this.currentSpeed += BRAKE_DECELERATION;
                } else {
                    this.currentSpeed = Math.min(MAX_FORWARD_SPEED, this.currentSpeed + ACCELERATION);
                }
            } else if (forward < -0.01f) {
                if (this.currentSpeed > 0.0) {
                    this.currentSpeed -= BRAKE_DECELERATION;
                } else {
                    this.currentSpeed = Math.max(-MAX_REVERSE_SPEED, this.currentSpeed - ACCELERATION * 0.8);
                }
            } else {
                this.currentSpeed *= FRICTION;
                if (Math.abs(this.currentSpeed) < 0.005) {
                    this.currentSpeed = 0.0;
                }
            }

            if (Math.abs(sideways) > 0.01f) {
                targetSteer = sideways > 0 ? -0.42f : 0.42f;
                if (Math.abs(this.currentSpeed) > 0.02) {
                    float reverseSign = this.currentSpeed >= 0.0 ? 1.0f : -1.0f;
                    float speedFactor = (float) MathHelper.clamp(Math.abs(this.currentSpeed) / 0.25, 0.35, 1.0);
                    float yawDelta = (sideways > 0 ? -TURN_SPEED : TURN_SPEED) * reverseSign * speedFactor;
                    this.setYaw(this.getYaw() + yawDelta);
                    driver.setYaw(driver.getYaw() + yawDelta);
                }
            }
        } else {
            this.currentSpeed *= FRICTION;
            if (Math.abs(this.currentSpeed) < 0.005) {
                this.currentSpeed = 0.0;
            }
        }

        this.steeringAngle = MathHelper.lerp(0.25f, this.steeringAngle, targetSteer);

        float rad = this.getYaw() * ((float) Math.PI / 180.0f);
        double vx = -MathHelper.sin(rad) * this.currentSpeed;
        double vz = MathHelper.cos(rad) * this.currentSpeed;
        double vy = this.isOnGround() ? -0.04 : Math.max(this.getVelocity().y - 0.08, -2.0);

        this.setVelocity(vx, vy, vz);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getWorld().isClient || this.isRemoved()) {
            return true;
        }
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        this.scheduleVelocityUpdate();
        this.hurtCooldown = 30;
        this.damageAccumulated += Math.max(amount, 1.5f);

        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_METAL_HIT, SoundCategory.BLOCKS, 0.8f, 1.2f);

        boolean creative = source.getAttacker() instanceof PlayerEntity p && p.getAbilities().creativeMode;
        if (creative || this.damageAccumulated >= 4.0f) {
            if (!creative && this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                this.dropItem(SimpleCarMod.CAR_ITEM);
            }
            this.discard();
        }
        return true;
    }

    public float getWheelRotation() {
        return this.wheelRotation;
    }

    public float getSteeringAngle() {
        return this.steeringAngle;
    }
}
