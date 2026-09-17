package dev.pulsevisual.gui.components;

import dev.pulsevisual.config.ConfigManager;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
    private final String name;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    protected Setting(String name, Supplier<T> getter, Consumer<T> setter) {
        this.name = name; this.getter = getter; this.setter = setter;
    }
    public String name() { return name; }
    public T get() { return getter.get(); }
    public void preview(T value) { setter.accept(value); }
    public void set(T value) { preview(value); ConfigManager.save(); }
}
