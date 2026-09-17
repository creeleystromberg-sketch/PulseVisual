package dev.pulsevisual.gui.components;

public final class ActionSetting extends Setting<Boolean> {
    private final Runnable action;
    public ActionSetting(String name,Runnable action){super(name,()->false,v->{});this.action=action;}
    public void run(){action.run();dev.pulsevisual.config.ConfigManager.save();}
}
