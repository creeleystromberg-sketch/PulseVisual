package dev.pulsevisual.util;

public final class ColorUtil {
    private ColorUtil() { }
    public static int alpha(int color, float alpha) {
        return (Math.max(0, Math.min(255, Math.round(alpha * 255))) << 24) | (color & 0xFFFFFF);
    }
    public static int mix(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
    public static int rainbow(float phase) { return 0xFF000000 | (java.awt.Color.HSBtoRGB(phase - (float)Math.floor(phase), .78f, 1f) & 0xFFFFFF); }
}
