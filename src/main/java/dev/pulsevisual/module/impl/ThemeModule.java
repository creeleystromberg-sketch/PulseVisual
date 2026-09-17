package dev.pulsevisual.module.impl;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;
import dev.pulsevisual.util.ThemePresets;

public final class ThemeModule extends Module {
    public ThemeModule() {
        super("theme", "Visual identity", "Theme");
        add(new NumberSetting("Menu Scale",.65f,1.5f,false,()->ConfigManager.get().theme.guiScale,v->ConfigManager.get().theme.guiScale=v).resetTo(1));
        add(new ModeSetting("Panel Style",new String[]{"Glass","Solid","Minimal"},()->ConfigManager.get().theme.style,v->ConfigManager.get().theme.style=v));
        add(new ModeSetting("Font",new String[]{"Default","Uniform","Bold"},()->ConfigManager.get().theme.font,v->ConfigManager.get().theme.font=v));
        add(new BooleanSetting("Menu Animations",()->ConfigManager.get().theme.animations,v->ConfigManager.get().theme.animations=v));
        add(new BooleanSetting("Text Shadow",()->ConfigManager.get().theme.textShadow,v->ConfigManager.get().theme.textShadow=v));
        add(new ModeSetting("Preset",new String[]{"Dark","Purple","Blue","Red","Black","Custom"},
                ()->ConfigManager.get().theme.preset,ThemePresets::apply));
        add(new ColorSetting("Primary Color",()->ConfigManager.get().theme.primary,v->ThemePresets.custom(t->t.primary=v)));
        add(new ColorSetting("Accent Color",()->ConfigManager.get().theme.accent,v->ThemePresets.custom(t->t.accent=v)));
        add(new ColorSetting("Background Color",()->ConfigManager.get().theme.background,v->ThemePresets.custom(t->t.background=v)));
        add(new ColorSetting("Text Color",()->ConfigManager.get().theme.text,v->ThemePresets.custom(t->t.text=v)));
        add(new ColorSetting("Enabled Modules",()->ConfigManager.get().theme.enabled,v->ThemePresets.custom(t->t.enabled=v)));
        add(new ColorSetting("Slider & Toggle",()->ConfigManager.get().theme.control,v->ThemePresets.custom(t->t.control=v)));
        add(new NumberSetting("Opacity",.45f,1,false,()->ConfigManager.get().theme.opacity,v->ConfigManager.get().theme.opacity=v).resetTo(.92f));
        add(new NumberSetting("Blur Intensity",0,1,false,()->ConfigManager.get().theme.blur,v->ConfigManager.get().theme.blur=v).resetTo(.55f));
        add(new NumberSetting("Corner Radius",0,16,false,()->ConfigManager.get().theme.rounding,v->ConfigManager.get().theme.rounding=v).resetTo(8));
    }
}
