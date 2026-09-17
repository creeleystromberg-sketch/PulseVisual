package dev.pulsevisual.gui.components;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(name, getter, setter);
    }
    public void toggle() { set(!get()); }
}
