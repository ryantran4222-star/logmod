package me.logwet.logmod.tools.trajectories.projectiles;

import net.minecraft.world.entity.projectile.AbstractArrow;

public interface IArrowProjectile extends IProjectile<AbstractArrow> {
    /**
     * Arrow gravity is 0.05F (higher than throwables at 0.03F)
     */
    default float getGravity() {
        return 0.05F;
    }

    /**
     * Air drag factor for arrows
     */
    default float getDrag() {
        return 0.99F;
    }

    /**
     * Water drag factor for arrows
     */
    default float getWaterDrag() {
        return 0.6F;
    }

    /**
     * Base velocity scaling when fully charged (1.0 second draw)
     * Minecraft uses: velocity = charge * 3.0F where charge is 0.0-1.0
     */
    default float getMaxVelocity() {
        return 3.0F;
    }

    /**
     * Inaccuracy factor (0.0F for perfect accuracy when fully charged)
     */
    default float getInaccuracy() {
        return 1.0F;
    }
}
