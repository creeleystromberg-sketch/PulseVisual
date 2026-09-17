package dev.pulsevisual.module.impl;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;

public final class ChinaHatModule extends Module {
    public ChinaHatModule() {
        super("player", "China Hat", "Player");
        add(new BooleanSetting("Enable China Hat",()->ConfigManager.get().hat.enabled,v->ConfigManager.get().hat.enabled=v));
        add(new ColorSetting("Color",()->ConfigManager.get().hat.color,v->ConfigManager.get().hat.color=v));
        add(new NumberSetting("Alpha",.05f,1,false,()->ConfigManager.get().hat.alpha,v->ConfigManager.get().hat.alpha=v));
        add(new NumberSetting("Size",.3f,1.5f,false,()->ConfigManager.get().hat.size,v->ConfigManager.get().hat.size=v));
        add(new NumberSetting("Height",-.3f,.8f,false,()->ConfigManager.get().hat.height,v->ConfigManager.get().hat.height=v));
        add(new NumberSetting("Rotation Speed",0,3,false,()->ConfigManager.get().hat.rotationSpeed,v->ConfigManager.get().hat.rotationSpeed=v));
        add(new ModeSetting("Style",new String[]{"Filled","Outline","Filled + Outline"},()->ConfigManager.get().hat.style,v->ConfigManager.get().hat.style=v));
        add(new BooleanSetting("Rainbow Mode",()->ConfigManager.get().hat.rainbow,v->ConfigManager.get().hat.rainbow=v));
        add(new BooleanSetting("Gradient Mode",()->ConfigManager.get().hat.gradient,v->ConfigManager.get().hat.gradient=v));
        add(new NumberSetting("Rainbow Speed",.1f,4,false,()->ConfigManager.get().hat.rainbowSpeed,v->ConfigManager.get().hat.rainbowSpeed=v));
    }
}
