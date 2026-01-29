package me.logwet.logmod.tools.trajectories.projectiles.arrow;

import java.util.ArrayList;
import java.util.List;
import me.logwet.logmod.mixin.common.trajectories.ProjectileInvoker;
import me.logwet.logmod.tools.trajectories.Trajectory;
import me.logwet.logmod.tools.trajectories.projectiles.IArrowProjectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractArrowProjectile implements IArrowProjectile {
    @Override
    public Trajectory calculateTrajectory(Player parent) {
        List<Vec3> trajectoryList = new ArrayList<>();
        BlockHitResult blockHitResult = null;

        AbstractArrow arrowEntity = this.getBaseEntity(parent.level, parent);

        // Shoot with full charge (1.0F charge = 3.0 velocity)
        // Using 0.0F inaccuracy for perfect accuracy prediction
        arrowEntity.shootFromRotation(
                parent,
                parent.xRot,
                parent.yRot,
                0.0F,
                this.getMaxVelocity(),
                0.0F);

        trajectoryList.add(arrowEntity.position());

        int tickCount;
        for (tickCount = 0; tickCount <= 1200; tickCount++) {
            HitResult hitResult =
                    ProjectileUtil.getHitResult(
                            arrowEntity,
                            (entity) -> {
                                if (!entity.isSpectator()
                                        && entity.isAlive()
                                        && entity.isPickable()) {
                                    return parent.isPassengerOfSameVehicle(entity);
                                } else {
                                    return false;
                                }
                            },
                            ClipContext.Block.OUTLINE);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                blockHitResult = (BlockHitResult) hitResult;
                break;
            }

            Vec3 velocity = arrowEntity.getDeltaMovement();
            double nextX = arrowEntity.getX() + velocity.x;
            double nextY = arrowEntity.getY() + velocity.y;
            double nextZ = arrowEntity.getZ() + velocity.z;
            
            ((ProjectileInvoker) arrowEntity).invokeUpdateRotation();

            Fluid fluid = parent.level.getFluidState(arrowEntity.blockPosition()).getType();
            boolean inWater = fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;

            float drag = inWater ? this.getWaterDrag() : this.getDrag();
            arrowEntity.setDeltaMovement(velocity.scale(drag));

            Vec3 scaledVelocity = arrowEntity.getDeltaMovement();
            arrowEntity.setDeltaMovement(
                    scaledVelocity.x,
                    scaledVelocity.y - (double) this.getGravity(),
                    scaledVelocity.z);

            arrowEntity.setPos(nextX, nextY, nextZ);

            trajectoryList.add(arrowEntity.position());
        }

        arrowEntity.kill();

        return new Trajectory(trajectoryList, blockHitResult, Trajectory.RenderType.FILLED, 3);
    }
}
