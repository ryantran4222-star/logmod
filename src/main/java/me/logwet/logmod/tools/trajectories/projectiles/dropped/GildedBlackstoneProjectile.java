package me.logwet.logmod.tools.trajectories.projectiles.dropped;

import java.util.function.Predicate;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GildedBlackstoneProjectile extends AbstractDroppedItemProjectile {
    public static GildedBlackstoneProjectile INSTANCE = new GildedBlackstoneProjectile();
    protected static Item itemType = Items.GILDED_BLACKSTONE;

    @Override
    public Predicate<Item> getTriggerPredicate() {
        return (item) -> item == itemType;
    }

    @Override
    public ItemEntity getBaseEntity(Level level, Player player) {
        return super.getBaseEntity(level, player, itemType);
    }
}
