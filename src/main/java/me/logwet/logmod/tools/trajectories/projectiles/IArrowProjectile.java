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
     * Base velocity scaling when fully charged
     * Bows: velocity = charge * 3.0F where charge is 0.0-1.0
     * Crossbows: velocity = 3.15F (slightly faster than bows)
     */
    default float getMaxVelocity(net.minecraft.world.entity.player.Player player) {
        net.minecraft.world.item.Item item = player.getMainHandItem().getItem();
        if (item == net.minecraft.world.item.Items.CROSSBOW) {
            return 3.15F;
        }
        return 3.0F; // Bow default
    }

    /**
     * Inaccuracy factor (0.0F for perfect accuracy when fully charged)
     */
    default float getInaccuracy() {
        return 1.0F;
    }
}
