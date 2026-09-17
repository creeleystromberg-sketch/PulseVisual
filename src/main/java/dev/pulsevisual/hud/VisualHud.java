package dev.pulsevisual.hud;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import dev.pulsevisual.effect.HitEffectManager;
import dev.pulsevisual.effect.WeaponEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;

public final class VisualHud {
    private VisualHud() { }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui || client.screen != null) return;
        VisualConfig config = ConfigManager.get();
        int cx = client.getWindow().getGuiScaledWidth() / 2;
        int cy = client.getWindow().getGuiScaledHeight() / 2;
        float flash = HitEffectManager.flashProgress();

        if (config.hitFlash && flash > 0) {
            int radius = Math.round(9 + (1 - flash) * 13);
            int alpha = Math.round(flash * 145 * Math.min(config.intensity, 1.5f));
            int tint = HitEffectManager.wasCritical() ? 0xFFD37B : 0xFFFFFF;
            int color = (Math.min(255, alpha) << 24) | tint;
            graphics.fill(cx - radius, cy - radius, cx - radius + 2, cy - radius + 5, color);
            graphics.fill(cx - radius, cy - radius, cx - radius + 5, cy - radius + 2, color);
            graphics.fill(cx + radius - 2, cy - radius, cx + radius, cy - radius + 5, color);
            graphics.fill(cx + radius - 5, cy - radius, cx + radius, cy - radius + 2, color);
            graphics.fill(cx - radius, cy + radius - 5, cx - radius + 2, cy + radius, color);
            graphics.fill(cx - radius, cy + radius - 2, cx - radius + 5, cy + radius, color);
            graphics.fill(cx + radius - 2, cy + radius - 5, cx + radius, cy + radius, color);
            graphics.fill(cx + radius - 5, cy + radius - 2, cx + radius, cy + radius, color);
        }

        if (config.weaponEffects && WeaponEffect.holdingItem(client.player.getMainHandItem())) {
            int y = client.getWindow().getGuiScaledHeight() - 28;
            int glow = (Math.round((0.38f + (float) Math.sin(System.currentTimeMillis() * 0.004) * 0.16f) * 180) << 24)
                    | (WeaponEffect.holdingWeapon(client.player.getMainHandItem())
                        ? (config.theme.accent & 0xFFFFFF) : 0xADD7CB);
            graphics.fill(cx - 12, y, cx + 12, y + 1, glow);
        }

        if (config.hudAnimation && flash > 0) {
            String label = HitEffectManager.wasCritical() ? "✦ CRITICAL" : "✧ HIT";
            int color = (Math.round(flash * 220) << 24) | (HitEffectManager.wasCritical()
                    ? 0xFFD37B : (config.theme.accent & 0xFFFFFF));
            int x = cx - client.font.width(label) / 2;
            int y = cy + 28 + Math.round((1 - flash) * 8);
            graphics.drawString(client.font, label, x, y, color, false);
        }
    }
}
