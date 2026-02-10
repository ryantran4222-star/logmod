package me.logwet.logmod.tools.trajectories.projectiles.throwable;

import java.util.function.Predicate;
import me.logwet.logmod.tools.trajectories.Trajectory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PearlProjectile extends AbstractThrowableProjectile {
    public static PearlProjectile INSTANCE = new PearlProjectile();

    @Override
    public Predicate<Item> getTriggerPredicate() {
        return (item) -> item == Items.ENDER_PEARL;
    }

    @Override
    public ThrowableProjectile getBaseEntity(Level level, Player player) {
        return new ThrownEnderpearl(level, player);
    }

    @Override
    public Trajectory calculateTrajectory(Player parent) {
        // Calculate normal trajectory (no player velocity)
        Trajectory normalTrajectory = calculateTrajectoryWithVelocity(parent, null);
        
        // Calculate jump-throw trajectory (simulating a jump)
        Vec3 jumpVelocity = calculateJumpVelocity(parent);
        Trajectory jumpThrowTrajectory = calculateTrajectoryWithVelocity(parent, jumpVelocity);
        
        // Return normal trajectory with jump-throw as alternate
        return new Trajectory(
            normalTrajectory.getTrajectory(),
            normalTrajectory.getBlockHitResult(),
            normalTrajectory.getEntityHitResult(),
            normalTrajectory.getRenderType(),
            normalTrajectory.getStartTick(),
            jumpThrowTrajectory
        );
    }

    private Vec3 calculateJumpVelocity(Player player) {
        // Get player's current movement velocity (walking/sprinting)
        Vec3 playerMovement = player.getDeltaMovement();
        
        // Calculate jump velocity (vanilla formula)
        double jumpYVelocity = 0.42F;
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
            jumpYVelocity += 0.1F * (player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getAmplifier() + 1);
        }
        
        // Return velocity TO BE ADDED to the pearl's base throw velocity
        // - Horizontal (X/Z): Player's movement velocity
        // - Vertical (Y): Jump velocity
        return new Vec3(
            playerMovement.x,
            jumpYVelocity,
            playerMovement.z
        );
    }
}
