package me.logwet.logmod.tools.trajectories;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TrajectoryRenderer {
    public static void drawLine(
            VertexConsumer vertexConsumer,
            Matrix4f matrix4f,
            Vec3 pos1,
            Vec3 pos2,
            float r,
            float g,
            float b,
            float a) {
        vertexConsumer
                .vertex(matrix4f, (float) pos1.x, (float) pos1.y, (float) pos1.z)
                .color(r, g, b, a)
                .endVertex();

        vertexConsumer
                .vertex(matrix4f, (float) pos2.x, (float) pos2.y, (float) pos2.z)
                .color(r, g, b, a)
                .endVertex();
    }

    private static double renderDottedLine(
            VertexConsumer vertexConsumer,
            Matrix4f matrix4f,
            Vec3 startPos,
            Vec3 endPos,
            double offset,
            float r,
            float g,
            float b,
            float a) {
        double stepSize = 1.0D;

        Vec3 dirVec = endPos.subtract(startPos);
        double l = dirVec.length();

        Vec3 stepVec = dirVec.normalize().scale(stepSize);

        boolean render = true;
        double j = offset;

        if (l > offset) {
            Vec3 offsetPos1 = startPos.add(dirVec.normalize().scale(offset / l));
            Vec3 offsetPos2;

            for (double i = offset; i < l; i += stepSize) {
                if (i <= l - stepSize) {
                    offsetPos2 = offsetPos1.add(stepVec);
                } else {
                    offsetPos2 = endPos;
                }

                if (render) {
                    drawLine(vertexConsumer, matrix4f, offsetPos1, offsetPos2, r, g, b, a);
                }

                offsetPos1 = offsetPos2;
                render = !render;

                j = i;
            }
        } else {
            while (j >= l) {
                j -= stepSize;
                render = !render;
            }
            render = !render;
        }

        double shift;

        if (!render) {
            shift = j + 2 * stepSize - l;
        } else {
            shift = j + stepSize - l;
        }

        return shift;
    }

    public static void renderTrajectory(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Vec3 entityPos,
            Trajectory trajectory,
            boolean isEnderPearl) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix4f = poseStack.last().pose();

        double offset = 0.0D;
        int tickLength = trajectory.getTrajectory().size();

        // All trajectory lines are 50% transparent
        float alpha = 0.5F;

        for (int i = trajectory.getStartTick(); i < tickLength; i++) {
            Vec3 prevPos = trajectory.getTrajectory().get(i - 1).subtract(entityPos);
            Vec3 pos = trajectory.getTrajectory().get(i).subtract(entityPos);

            float r;
            float g;
            float b;

            float colorFactor =
                    Mth.clamp(
                            (float) i / ((float) tickLength - trajectory.getStartTick()),
                            0.0F,
                            1.0F);
            r = colorFactor;
            g = (1.0F - colorFactor);
            b = 0.0F;

            if (trajectory.getRenderType() == Trajectory.RenderType.FILLED) {
                drawLine(vertexConsumer, matrix4f, prevPos, pos, r, g, b, alpha);
            } else if (trajectory.getRenderType() == Trajectory.RenderType.DOTTED) {
                offset = renderDottedLine(vertexConsumer, matrix4f, prevPos, pos, offset, r, g, b, alpha);
            }
        }
    }
}
