package dev.pulsevisual;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.effect.HitEffectManager;
import dev.pulsevisual.effect.WeaponEffect;
import dev.pulsevisual.gui.SettingsScreen;
import dev.pulsevisual.hud.VisualHud;
import dev.pulsevisual.render.ChinaHatRenderer;
import dev.pulsevisual.render.TrailRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public final class PulseVisualClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        dev.pulsevisual.update.AutoUpdater.start();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client->dev.pulsevisual.update.AutoUpdater.onStopping());
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("pulsevisual", "settings"));
        KeyMapping settingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.pulsevisual.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (settingsKey.consumeClick()) client.setScreen(new SettingsScreen(client.screen));
            WeaponEffect.tick(client);
            TrailRenderer.tick(client);
            dev.pulsevisual.animation.SwingAnimator.tick(client);
            dev.pulsevisual.render.PearlTracer.tick(client);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() && player instanceof net.minecraft.client.player.LocalPlayer
                    && entity instanceof LivingEntity) {
                boolean critical = player.fallDistance > 0 && !player.onGround()
                        && !player.isInWater() && !player.onClimbable() && !player.isPassenger();
                HitEffectManager.onAttack(entity, critical);
            }
            return InteractionResult.PASS;
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("pulsevisual", "visual_hud"), VisualHud::render);
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            ChinaHatRenderer.render(context);
            TrailRenderer.render(context);
            dev.pulsevisual.render.PearlTracer.render(context);
        });
    }
}
