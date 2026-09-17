package dev.pulsevisual.module.impl;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;

public final class HudModule extends Module {
    public HudModule() {
        super("hud", "Crosshair & HUD", "HUD");
        add(new BooleanSetting("Customize vanilla crosshair",()->ConfigManager.get().customizeCrosshair,v->ConfigManager.get().customizeCrosshair=v));
        add(new NumberSetting("Crosshair Size",.5f,2.5f,false,()->ConfigManager.get().crosshairSize,v->ConfigManager.get().crosshairSize=v).resetTo(1));
        add(new NumberSetting("Crosshair Opacity",.15f,1,false,()->ConfigManager.get().crosshairOpacity,v->ConfigManager.get().crosshairOpacity=v).resetTo(1));
        add(new ActionSetting("Reset Vanilla Crosshair",()->{var c=ConfigManager.get();c.customizeCrosshair=false;c.crosshairSize=1;c.crosshairOpacity=1;}));
        add(new BooleanSetting("Animated HUD",()->ConfigManager.get().hudAnimation,v->ConfigManager.get().hudAnimation=v));
    }
}
