package dev.pulsevisual.module.impl;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.components.BooleanSetting;
import dev.pulsevisual.gui.components.NumberSetting;
import dev.pulsevisual.gui.components.ModeSetting;
import dev.pulsevisual.module.Module;

public final class GeneralModule extends Module {
    public GeneralModule() {
        super("general", "Combat & item cosmetics", "General");
        add(new BooleanSetting("Normal hit particles", () -> ConfigManager.get().normalHitParticles, v -> ConfigManager.get().normalHitParticles=v));
        add(new BooleanSetting("Critical hit particles", () -> ConfigManager.get().criticalHitParticles, v -> ConfigManager.get().criticalHitParticles=v));
        add(new BooleanSetting("Hit flash", () -> ConfigManager.get().hitFlash, v -> ConfigManager.get().hitFlash=v));
        add(new BooleanSetting("Item & weapon aura", () -> ConfigManager.get().weaponEffects, v -> ConfigManager.get().weaponEffects=v));
        add(new NumberSetting("Particle count", 0, 64, true, () -> (float)ConfigManager.get().particleCount, v -> ConfigManager.get().particleCount=v.intValue()));
        add(new NumberSetting("Intensity", .1f, 2, false, () -> ConfigManager.get().intensity, v -> ConfigManager.get().intensity=v));
        add(new ModeSetting("Hit particles",new String[]{"Default","Spark","Crit","End Rod","Heart","Flame"},()->ConfigManager.get().hit.particle,v->ConfigManager.get().hit.particle=v));
        add(new ModeSetting("Hit shape",new String[]{"Burst","Ring","Upward"},()->ConfigManager.get().hit.shape,v->ConfigManager.get().hit.shape=v));
        add(new NumberSetting("Particle spread",.1f,2,false,()->ConfigManager.get().hit.spread,v->ConfigManager.get().hit.spread=v).resetTo(1));
        add(new NumberSetting("Particle speed",0,3,false,()->ConfigManager.get().hit.speed,v->ConfigManager.get().hit.speed=v).resetTo(1));
        add(new NumberSetting("Hit height",.1f,1,false,()->ConfigManager.get().hit.height,v->ConfigManager.get().hit.height=v).resetTo(.55f));
        add(new NumberSetting("Flash duration",.05f,1,false,()->ConfigManager.get().hit.flashDuration,v->ConfigManager.get().hit.flashDuration=v).resetTo(.26f));
        add(new BooleanSetting("Extra sparks",()->ConfigManager.get().hit.extraSparks,v->ConfigManager.get().hit.extraSparks=v));
        add(new BooleanSetting("Automatic GitHub updates",()->ConfigManager.get().automaticUpdates,v->ConfigManager.get().automaticUpdates=v));
        add(new dev.pulsevisual.gui.components.ActionSetting("Check for updates",dev.pulsevisual.update.AutoUpdater::checkNow));
        add(new dev.pulsevisual.gui.components.ActionSetting("Show update status",()->{
            var mc=net.minecraft.client.Minecraft.getInstance();
            if(mc.player!=null)mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("PulseVisual: "+dev.pulsevisual.update.AutoUpdater.status()),false);
        }));
    }
}
