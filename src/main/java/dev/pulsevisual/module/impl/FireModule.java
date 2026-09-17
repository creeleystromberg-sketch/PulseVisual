package dev.pulsevisual.module.impl;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;

public final class FireModule extends Module {
    public FireModule(){
        super("fire","Fire overlay / first person only","Fire");
        add(new ModeSetting("Preset",new String[]{"Vanilla","Low","Very Low","Hidden","Custom"},
                ()->ConfigManager.get().fire.preset,v->{
                    var c=ConfigManager.get().fire;c.preset=v;
                    c.size=switch(v){case "Vanilla"->100;case "Low"->50;case "Very Low"->20;case "Hidden"->0;default->c.size;};
                }));
        add(new NumberSetting("Fire Height (%)",0,100,true,()->ConfigManager.get().fire.size,v->{
            ConfigManager.get().fire.size=v;ConfigManager.get().fire.preset="Custom";
        }).resetTo(100));
    }
}
