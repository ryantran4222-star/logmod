package me.logwet.logmod.tools.trajectories.projectiles.dropped;

import java.util.ArrayList;
import java.util.List;
import me.logwet.logmod.mixin.common.trajectories.EntityAccessor;
import me.logwet.logmod.tools.trajectories.Trajectory;
import me.logwet.logmod.tools.trajectories.projectiles.IDroppedItemProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractDroppedItemProjectile implements IDroppedItemProjectile {
    protected ItemEntity getBaseEntity(Level level, Player player, Item item) {
        return new ItemEntity(
                level,
                player.getX(),
                player.getEyeY() - 0.30000001192092896D,
                player.getZ(),
                new ItemStack(item));
    }

    @Override
    public Trajectory calculateTrajectory(Player parent) {
        List<Vec3> trajectoryList = new ArrayList<>();

        ItemEntity projectileEntity = this.getBaseEntity(parent.level, parent);
        projectileEntity.setPickUpDelay(300);
        projectileEntity.setThrower(parent.getUUID());

        {
            float g = Mth.sin(parent.xRot * 0.017453292F);
            float j = Mth.cos(parent.xRot * 0.017453292F);
            float k = Mth.sin(parent.yRot * 0.017453292F);
            float l = Mth.cos(parent.yRot * 0.017453292F);

            projectileEntity.setDeltaMovement(
                    (double) (-k * j * 0.3F),
                    -g * 0.3F + 0.1F,
                    (double) (l * j * 0.3F));

            trajectoryList.add(projectileEntity.position());
        }

        int tickCount;
        for (tickCount = 0; tickCount <= 300; tickCount++) {
            {
                projectileEntity.xo = projectileEntity.getX();
                projectileEntity.yo = projectileEntity.getY();
                projectileEntity.zo = projectileEntity.getZ();
                Vec3 vec3 = projectileEntity.getDeltaMovement();

                Fluid fluid =
                        parent.level.getFluidState(projectileEntity.blockPosition()).getType();
                boolean inWater = fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;

                if (inWater) {
                    projectileEntity.setDeltaMovement(
                            vec3.x * 0.9900000095367432D,
                            vec3.y + (double) (vec3.y < 0.05999999865889549D ? 5.0E-4F : 0.0F),
                            vec3.z * 0.9900000095367432D);
                } else {
                    projectileEntity.setDeltaMovement(vec3.add(0.0D, -0.04D, 0.0D));
                }

                if (!((EntityAccessor) projectileEntity).getOnGround()
                        || Entity.getHorizontalDistanceSqr(projectileEntity.getDeltaMovement())
                                > 9.999999747378752E-6D
                        || tickCount % 4 == 0) {
                    projectileEntity.move(MoverType.SELF, projectileEntity.getDeltaMovement());
                    float f = 0.98F;
                    if (((EntityAccessor) projectileEntity).getOnGround()) {
                        f =
                                projectileEntity
                                                .level
                                                .getBlockState(
                                                        new BlockPos(
                                                                projectileEntity.getX(),
                                                                projectileEntity.getY() - 1.0D,
                                                                projectileEntity.getZ()))
                                                .getBlock()
                                                .getFriction()
                                        * 0.98F;
                    }

                    projectileEntity.setDeltaMovement(
                            projectileEntity.getDeltaMovement().multiply(f, 0.98D, f));
                    if (((EntityAccessor) projectileEntity).getOnGround()) {
                        projectileEntity.setDeltaMovement(
                                projectileEntity.getDeltaMovement().multiply(1.0D, -0.5D, 1.0D));
                    }
                }
            }

            trajectoryList.add(projectileEntity.position());

            if (projectileEntity.getDeltaMovement().lengthSqr() < 1.0E-5D) {
                break;
            }
        }

        projectileEntity.kill();

        return new Trajectory(trajectoryList, null, Trajectory.RenderType.FILLED, 2);
    }
}
