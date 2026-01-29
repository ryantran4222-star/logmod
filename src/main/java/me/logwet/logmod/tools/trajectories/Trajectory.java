package me.logwet.logmod.tools.trajectories;

import java.util.List;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Trajectory {
    private final List<Vec3> trajectory;
    private final BlockHitResult blockHitResult;
    private final EntityHitResult entityHitResult;
    private final RenderType renderType;
    private final int startTick;

    public Trajectory(
            List<Vec3> trajectory,
            @Nullable BlockHitResult blockHitResult,
            RenderType renderType,
            int startTick) {
        this(trajectory, blockHitResult, null, renderType, startTick);
    }

    public Trajectory(
            List<Vec3> trajectory,
            @Nullable BlockHitResult blockHitResult,
            @Nullable EntityHitResult entityHitResult,
            RenderType renderType,
            int startTick) {
        this.trajectory = trajectory;
        this.blockHitResult = blockHitResult;
        this.entityHitResult = entityHitResult;
        this.renderType = renderType;
        this.startTick = startTick;
    }

    public List<Vec3> getTrajectory() {
        return trajectory;
    }

    public BlockHitResult getBlockHitResult() {
        return blockHitResult;
    }

    public EntityHitResult getEntityHitResult() {
        return entityHitResult;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public int getStartTick() {
        return startTick;
    }

    public enum RenderType {
        FILLED,
        DOTTED
    }
}
