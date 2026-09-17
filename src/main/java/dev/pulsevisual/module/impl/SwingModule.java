package dev.pulsevisual.module.impl;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.components.ModeSetting;
import dev.pulsevisual.gui.components.NumberSetting;
import dev.pulsevisual.module.Module;
import dev.pulsevisual.gui.components.Setting;
import java.util.List;

public final class SwingModule extends Module {
    public SwingModule() {
        super("swing", "Client-side swing animation", "Swing");
        add(new ModeSetting("Animation", new String[]{"Vanilla","Custom"},
                () -> ConfigManager.get().swing.preset, v -> {ConfigManager.get().swing.preset=v;ConfigManager.get().swing.enabled=true;}));
        add(new NumberSetting("Swing Speed", .25f,3,false,()->ConfigManager.get().swing.speed,v->ConfigManager.get().swing.speed=v).resetTo(1));
        add(new NumberSetting("Swing Duration", .15f,1.2f,false,()->ConfigManager.get().swing.duration,v->ConfigManager.get().swing.duration=v).resetTo(.42f));
        add(new NumberSetting("Swing Strength",0,2,false,()->ConfigManager.get().swing.strength,v->ConfigManager.get().swing.strength=v).resetTo(1));
        add(new NumberSetting("Rotation Z / tilt",-120,120,false,()->ConfigManager.get().swing.rotation,v->ConfigManager.get().swing.rotation=v).resetTo(30));
        add(new NumberSetting("Rotation X",-90,90,false,()->ConfigManager.get().swing.rotationX,v->ConfigManager.get().swing.rotationX=v).resetTo(-7.5f));
        add(new NumberSetting("Rotation Y",-90,90,false,()->ConfigManager.get().swing.rotationY,v->ConfigManager.get().swing.rotationY=v).resetTo(7.5f));
        add(new NumberSetting("Peak timing",.15f,.8f,false,()->ConfigManager.get().swing.peak,v->ConfigManager.get().swing.peak=v).resetTo(.36f));
        add(new NumberSetting("Offset X",-1,1,false,()->ConfigManager.get().swing.offsetX,v->ConfigManager.get().swing.offsetX=v));
        add(new NumberSetting("Offset Y",-1,1,false,()->ConfigManager.get().swing.offsetY,v->ConfigManager.get().swing.offsetY=v));
        add(new NumberSetting("Offset Z",-1,1,false,()->ConfigManager.get().swing.offsetZ,v->ConfigManager.get().swing.offsetZ=v));
        add(new ModeSetting("Easing",new String[]{"Linear","Smoothstep","Sine","Cubic","Quint"},
                ()->ConfigManager.get().swing.easing,v->ConfigManager.get().swing.easing=v));
        add(new NumberSetting("Smoothness",0,1,false,()->ConfigManager.get().swing.smoothness,v->ConfigManager.get().swing.smoothness=v).resetTo(1));
    }
    @Override
    public List<Setting<?>> visibleSettings() {
        return ConfigManager.get().swing.preset.equals("Custom") ? settings() : settings().subList(0,1);
    }
}
