package me.logwet.logmod;

import com.mojang.blaze3d.platform.InputConstants.Type;
import me.logwet.logmod.commands.RootCommand;
import me.logwet.logmod.commands.client.ClientToggleCommand;
import me.logwet.logmod.tools.hud.PlayerAttributeRenderer;
import me.logwet.logmod.tools.hud.SpeedRenderer;
import me.logwet.logmod.tools.hud.WeaponAttributeRenderer;
import me.logwet.logmod.tools.overlay.OverlayRenderer;
import me.logwet.logmod.tools.paths.PathHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class LogModClient implements ClientModInitializer {
    private static int jumpThrowCooldown = 0;
    private static net.minecraft.world.InteractionHand pendingPearlHand = null;

    @Override
    public void onInitializeClient() {
        OverlayRenderer.registerRenderer(new SpeedRenderer());
        OverlayRenderer.registerRenderer(new PlayerAttributeRenderer());
        OverlayRenderer.registerRenderer(new WeaponAttributeRenderer());

        RootCommand.registerClientCommand(
                new ClientToggleCommand(
                        "renderSpawner",
                        "spawner info renderer",
                        LogModData::toggleRenderSpawnersEnabled));
        RootCommand.registerClientCommand(
                new ClientToggleCommand("hud", "info HUD", LogModData::toggleHudEnabled));
        RootCommand.registerClientCommand(
                new ClientToggleCommand(
                        "projectiles",
                        "projectiles renderer",
                        LogModData::toggleProjectilesEnabled));
        RootCommand.registerClientCommand(
                new ClientToggleCommand("paths", "paths renderer", LogModData::togglePathsEnabled));
        RootCommand.registerClientCommand(
                new ClientToggleCommand(
                        "piglins", "piglins renderer", LogModData::togglePiglinsEnabled));
        RootCommand.registerClientCommand(
                new ClientToggleCommand(
                        "health", "health renderer", LogModData::toggleHealthEnabled));

        RootCommand.registerClient(ClientCommandManager.DISPATCHER);

        KeyMapping buildPathKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.logmod.path.build",
                                Type.KEYSYM,
                                GLFW.GLFW_KEY_INSERT,
                                "key.category.logmod.path"));

        KeyMapping deletePathKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.logmod.path.delete",
                                Type.KEYSYM,
                                GLFW.GLFW_KEY_END,
                                "key.category.logmod.path"));

        KeyMapping jumpThrowKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.logmod.jumpthrow",
                                Type.KEYSYM,
                                GLFW.GLFW_KEY_V,
                                "key.category.logmod.general"));

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (client.player != null) {
                        // Handle delayed pearl throw
                        if (jumpThrowCooldown > 0) {
                            jumpThrowCooldown--;
                            if (jumpThrowCooldown == 0 && pendingPearlHand != null && client.gameMode != null) {
                                client.gameMode.useItem(client.player, client.level, pendingPearlHand);
                                client.player.displayClientMessage(
                                        new TextComponent("§aJump-throw!"), true);
                                pendingPearlHand = null;
                            }
                        }

                        while (buildPathKey.consumeClick()) {
                            PathHandler.buildPath(client);

                            client.player.displayClientMessage(
                                    new TextComponent("Adding node to path!"), false);
                        }

                        while (deletePathKey.consumeClick()) {
                            PathHandler.deletePath(client);

                            client.player.displayClientMessage(
                                    new TextComponent("Deleting path!"), false);
                        }

                        while (jumpThrowKey.consumeClick()) {
                            performJumpThrow(client);
                        }
                    }
                });

        LogMod.log(Level.INFO, "Client class initialized!");
    }

    private static void performJumpThrow(Minecraft client) {
        if (client.player == null || client.gameMode == null) {
            return;
        }

        // Check if player is holding an ender pearl
        if (client.player.getMainHandItem().getItem() != net.minecraft.world.item.Items.ENDER_PEARL &&
            client.player.getOffhandItem().getItem() != net.minecraft.world.item.Items.ENDER_PEARL) {
            client.player.displayClientMessage(
                    new TextComponent("§cYou must be holding an ender pearl!"), true);
            return;
        }

        // Check if player can jump (is on ground or in water)
        if (!((me.logwet.logmod.mixin.common.trajectories.EntityAccessor) client.player).getOnGround() 
            && !client.player.isInWater()) {
            client.player.displayClientMessage(
                    new TextComponent("§cYou must be on the ground or in water!"), true);
            return;
        }

        // Determine which hand has the ender pearl
        net.minecraft.world.InteractionHand hand = 
            client.player.getMainHandItem().getItem() == net.minecraft.world.item.Items.ENDER_PEARL 
                ? net.minecraft.world.InteractionHand.MAIN_HAND 
                : net.minecraft.world.InteractionHand.OFF_HAND;

        // Set jump velocity manually
        double jumpYVelocity = 0.42F;
        if (client.player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
            jumpYVelocity += 0.1F * (client.player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getAmplifier() + 1);
        }
        net.minecraft.world.phys.Vec3 currentVelocity = client.player.getDeltaMovement();
        client.player.setDeltaMovement(currentVelocity.x, jumpYVelocity, currentVelocity.z);
        
        // Queue pearl throw for 1 tick later (after jump applies)
        jumpThrowCooldown = 1;
        pendingPearlHand = hand;
    }
}
