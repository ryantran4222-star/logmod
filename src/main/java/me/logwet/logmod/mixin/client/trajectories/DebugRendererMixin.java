package me.logwet.logmod.mixin.client.trajectories;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.UUID;
import me.logwet.logmod.LogMod;
import me.logwet.logmod.LogModData;
import me.logwet.logmod.tools.BoxRenderer;
import me.logwet.logmod.tools.trajectories.Trajectory;
import me.logwet.logmod.tools.trajectories.TrajectoryRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            double x,
            double y,
            double z,
            CallbackInfo ci) {
        UUID uuid = Minecraft.getInstance().gameRenderer.getMainCamera().getEntity().getUUID();

        Trajectory trajectory;

        if (LogMod.shouldRender()
                && LogModData.isProjectilesEnabled()
                && (trajectory = LogModData.getTrajectory(uuid)) != null) {
            Vec3 playerPos = new Vec3(x, y, z);

            // Check if player is holding an ender pearl
            Entity cameraEntity = Minecraft.getInstance().gameRenderer.getMainCamera().getEntity();
            boolean isEnderPearl = false;
            if (cameraEntity instanceof net.minecraft.world.entity.player.Player) {
                net.minecraft.world.entity.player.Player player = (net.minecraft.world.entity.player.Player) cameraEntity;
                isEnderPearl = player.getMainHandItem().getItem() == net.minecraft.world.item.Items.ENDER_PEARL ||
                               player.getOffhandItem().getItem() == net.minecraft.world.item.Items.ENDER_PEARL;
            }

            // Render normal trajectory
            TrajectoryRenderer.renderTrajectory(poseStack, bufferSource, playerPos, trajectory, isEnderPearl);

            BlockHitResult blockHitResult = trajectory.getBlockHitResult();
            EntityHitResult entityHitResult = trajectory.getEntityHitResult();

            if (entityHitResult != null) {
                // Render orange box for entity hit
                Entity hitEntity = entityHitResult.getEntity();
                AABB entityBox = hitEntity.getBoundingBox().move(playerPos.scale(-1));

                BoxRenderer.renderBox(
                        poseStack,
                        bufferSource,
                        entityBox,
                        1.0F,
                        0.5F,
                        0.0F);
            } else if (blockHitResult != null) {
                // Render magenta box for block hit
                BlockPos blockPos = blockHitResult.getBlockPos();

                BoxRenderer.renderBox(
                        poseStack,
                        bufferSource,
                        AABB.unitCubeFromLowerCorner(
                                Vec3.atLowerCornerOf(blockPos).subtract(playerPos)),
                        1.0F,
                        0.0F,
                        1.0F);
            }

            // Render alternate trajectory (jump-throw) if it exists
            Trajectory alternateTrajectory = trajectory.getAlternateTrajectory();
            if (alternateTrajectory != null) {
                TrajectoryRenderer.renderTrajectory(poseStack, bufferSource, playerPos, alternateTrajectory, isEnderPearl);

                BlockHitResult altBlockHitResult = alternateTrajectory.getBlockHitResult();
                EntityHitResult altEntityHitResult = alternateTrajectory.getEntityHitResult();

                if (altEntityHitResult != null) {
                    // Render orange box for entity hit
                    Entity hitEntity = altEntityHitResult.getEntity();
                    AABB entityBox = hitEntity.getBoundingBox().move(playerPos.scale(-1));

                    BoxRenderer.renderBox(
                            poseStack,
                            bufferSource,
                            entityBox,
                            1.0F,
                            0.5F,
                            0.0F);
                } else if (altBlockHitResult != null) {
                    // Render ORANGE box for jump-throw block hit
                    BlockPos blockPos = altBlockHitResult.getBlockPos();

                    BoxRenderer.renderBox(
                            poseStack,
                            bufferSource,
                            AABB.unitCubeFromLowerCorner(
                                    Vec3.atLowerCornerOf(blockPos).subtract(playerPos)),
                            1.0F,
                            0.5F,
                            0.0F);
                }
            }
        }
    }
}
