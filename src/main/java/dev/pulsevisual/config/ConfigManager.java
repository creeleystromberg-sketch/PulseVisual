package dev.pulsevisual.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pulsevisual.json");
    private static VisualConfig config = new VisualConfig();

    private ConfigManager() { }

    public static VisualConfig get() { return config; }

    public static void resetAll() { config = new VisualConfig(); save(); }

    public static void resetModule(String id) {
        switch (id) {
            case "general" -> { VisualConfig d = new VisualConfig(); config.normalHitParticles=d.normalHitParticles; config.criticalHitParticles=d.criticalHitParticles; config.hitFlash=d.hitFlash; config.weaponEffects=d.weaponEffects; config.particleCount=d.particleCount; config.intensity=d.intensity; config.hit=d.hit; }
            case "view_model" -> config.viewModel = new VisualConfig.ViewModel();
            case "swing" -> config.swing = new VisualConfig.Swing();
            case "fire" -> config.fire = new VisualConfig.Fire();
            case "pearl" -> config.pearl = new VisualConfig.Pearl();
            case "player" -> config.hat = new VisualConfig.Hat();
            case "trails" -> config.trail = new VisualConfig.Trail();
            case "hud" -> { VisualConfig d = new VisualConfig(); config.hudAnimation=d.hudAnimation; config.customizeCrosshair=false; config.crosshairSize=d.crosshairSize; config.crosshairOpacity=d.crosshairOpacity; }
            case "theme" -> config.theme = new VisualConfig.Theme();
            default -> { }
        }
        save();
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        try {
            VisualConfig loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), VisualConfig.class);
            if (loaded != null) config = loaded;
            config.sanitize();
        } catch (Exception exception) {
            System.err.println("[PulseVisual] Could not read config: " + exception.getMessage());
        }
    }

    public static void save() {
        config.sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("[PulseVisual] Could not save config: " + exception.getMessage());
        }
    }
}
