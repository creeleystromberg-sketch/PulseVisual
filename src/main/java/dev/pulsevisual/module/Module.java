package dev.pulsevisual.module;

import dev.pulsevisual.gui.components.Setting;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String id, title, category;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String id, String title, String category) {
        this.id = id; this.title = title; this.category = category;
    }
    protected <T extends Setting<?>> T add(T setting) { settings.add(setting); return setting; }
    public String id() { return id; }
    public String title() { return title; }
    public String category() { return category; }
    public List<Setting<?>> settings() { return settings; }
    public List<Setting<?>> visibleSettings() { return settings; }
}
