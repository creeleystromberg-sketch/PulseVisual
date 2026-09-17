package dev.pulsevisual.util;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import java.util.function.Consumer;

public final class ThemePresets {
    private ThemePresets() { }
    public static void custom(Consumer<VisualConfig.Theme> edit) {
        VisualConfig.Theme theme=ConfigManager.get().theme; edit.accept(theme); theme.preset="Custom";
    }
    public static void apply(String name) {
        VisualConfig.Theme t=ConfigManager.get().theme;
        t.preset=name;
        switch(name) {
            case "Purple" -> colors(t,0xFF251C36,0xFFC194FF,0xFF0D0915,0xFFF5EFFF,0xFF65438F);
            case "Blue" -> colors(t,0xFF17283C,0xFF6CB9FF,0xFF08111D,0xFFEAF4FF,0xFF2B628F);
            case "Red" -> colors(t,0xFF331C24,0xFFFF7F91,0xFF15090C,0xFFFFEDF0,0xFF913C53);
            case "Black" -> colors(t,0xFF151515,0xFFFFFFFF,0xFF030303,0xFFF4F4F4,0xFF555555);
            case "Dark" -> colors(t,0xFF172332,0xFF85EAF3,0xFF080D16,0xFFE5EDF4,0xFF246E78);
            default -> { }
        }
    }
    private static void colors(VisualConfig.Theme t,int primary,int accent,int background,int text,int enabled) {
        t.primary=primary; t.accent=accent; t.background=background; t.text=text; t.enabled=enabled; t.control=accent;
    }
}
