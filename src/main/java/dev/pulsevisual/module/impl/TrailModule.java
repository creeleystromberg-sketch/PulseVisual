package dev.pulsevisual.module.impl;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;

public final class TrailModule extends Module {
    public TrailModule() {
        super("trails", "Motion Trails", "Trails");
        add(new BooleanSetting("Enable Trails",()->ConfigManager.get().trail.enabled,v->ConfigManager.get().trail.enabled=v));
        add(new ModeSetting("Mode",new String[]{"Line","Ribbon","Glow","Gradient","Rainbow","Pulse"},()->ConfigManager.get().trail.mode,v->ConfigManager.get().trail.mode=v));
        add(new ColorSetting("Trail Color",()->ConfigManager.get().trail.color,v->ConfigManager.get().trail.color=v));
        add(new ColorSetting("Secondary Color",()->ConfigManager.get().trail.secondaryColor,v->ConfigManager.get().trail.secondaryColor=v));
        add(new NumberSetting("Width",.04f,1.4f,false,()->ConfigManager.get().trail.width,v->ConfigManager.get().trail.width=v).resetTo(.38f));
        add(new NumberSetting("Length (seconds)",.4f,8,false,()->ConfigManager.get().trail.length,v->ConfigManager.get().trail.length=v).resetTo(2.7f));
        add(new NumberSetting("Fade Speed",.25f,3,false,()->ConfigManager.get().trail.fadeSpeed,v->ConfigManager.get().trail.fadeSpeed=v).resetTo(1));
        add(new NumberSetting("Alpha",.05f,1,false,()->ConfigManager.get().trail.alpha,v->ConfigManager.get().trail.alpha=v).resetTo(.8f));
        add(new NumberSetting("Glow Strength",0,2,false,()->ConfigManager.get().trail.glowStrength,v->ConfigManager.get().trail.glowStrength=v).resetTo(.55f));
        add(new NumberSetting("Smoothness",0,1,false,()->ConfigManager.get().trail.smoothness,v->ConfigManager.get().trail.smoothness=v).resetTo(.65f));
        add(new NumberSetting("Rainbow Speed",.1f,4,false,()->ConfigManager.get().trail.rainbowSpeed,v->ConfigManager.get().trail.rainbowSpeed=v).resetTo(1));
        add(new BooleanSetting("Gradient",()->ConfigManager.get().trail.gradient,v->ConfigManager.get().trail.gradient=v));
        add(new NumberSetting("Height",.03f,1.5f,false,()->ConfigManager.get().trail.height,v->ConfigManager.get().trail.height=v).resetTo(.13f));
        add(new NumberSetting("Point spacing",.02f,.3f,false,()->ConfigManager.get().trail.minDistance,v->ConfigManager.get().trail.minDistance=v).resetTo(.05f));
        add(new NumberSetting("Tail taper",0,1,false,()->ConfigManager.get().trail.taper,v->ConfigManager.get().trail.taper=v).resetTo(1));
        add(new NumberSetting("Pulse speed",.1f,4,false,()->ConfigManager.get().trail.pulseSpeed,v->ConfigManager.get().trail.pulseSpeed=v).resetTo(1));
        add(new NumberSetting("Glow width",1,4,false,()->ConfigManager.get().trail.glowWidth,v->ConfigManager.get().trail.glowWidth=v).resetTo(2));
        add(new BooleanSetting("Only while sprinting",()->ConfigManager.get().trail.sprintOnly,v->ConfigManager.get().trail.sprintOnly=v));
        add(new BooleanSetting("Only on ground",()->ConfigManager.get().trail.groundOnly,v->ConfigManager.get().trail.groundOnly=v));
        add(new NumberSetting("Saved Points",8,160,true,()->(float)ConfigManager.get().trail.maxPoints,v->ConfigManager.get().trail.maxPoints=v.intValue()).resetTo(56));
    }
}
