package dev.pulsevisual.module.impl;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;

public final class PearlModule extends Module {
    private static VisualConfig.Pearl c(){return ConfigManager.get().pearl;}
    public PearlModule(){
        super("pearl","Pearl Tracer","Pearl Tracer");
        add(new BooleanSetting("Enable Pearl Tracer",()->c().enabled,v->c().enabled=v));
        add(new ModeSetting("Mode",new String[]{"Line","Smooth Line","Glow","Gradient","Rainbow"},()->c().mode,v->c().mode=v));
        add(new BooleanSetting("Use theme color",()->c().themeColor,v->c().themeColor=v));
        add(new ColorSetting("Color",()->c().themeColor?ConfigManager.get().theme.accent:c().color,v->{c().color=v;c().themeColor=false;}));
        add(new ColorSetting("Secondary Color",()->c().secondaryColor,v->c().secondaryColor=v));
        add(new NumberSetting("Alpha",0,1,false,()->c().alpha,v->c().alpha=v).resetTo(.85f));
        add(new NumberSetting("Width",.01f,.15f,false,()->c().width,v->c().width=v).resetTo(.035f));
        add(new NumberSetting("Length (seconds)",.25f,8,false,()->c().length,v->c().length=v).resetTo(3));
        add(new NumberSetting("Fade Speed",.25f,3,false,()->c().fadeSpeed,v->c().fadeSpeed=v).resetTo(1));
        add(new BooleanSetting("Glow",()->c().glow,v->c().glow=v));
        add(new BooleanSetting("Rainbow",()->c().rainbow,v->c().rainbow=v));
        add(new BooleanSetting("Gradient",()->c().gradient,v->c().gradient=v));
        add(new BooleanSetting("Pulse",()->c().pulse,v->c().pulse=v));
        add(new BooleanSetting("Clear after teleport",()->c().clearOnTeleport,v->c().clearOnTeleport=v));
    }
}
