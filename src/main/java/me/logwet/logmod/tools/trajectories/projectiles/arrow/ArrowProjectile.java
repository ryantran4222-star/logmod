package me.logwet.logmod.tools.trajectories.projectiles.arrow;

import java.util.function.Predicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ArrowProjectile extends AbstractArrowProjectile {
    public static ArrowProjectile INSTANCE = new ArrowProjectile();

    @Override
    public Predicate<Item> getTriggerPredicate() {
        return (item) -> item == Items.BOW || item == Items.CROSSBOW;
    }

    @Override
    public AbstractArrow getBaseEntity(Level level, Player player) {
        return new Arrow(level, player);
    }
}
