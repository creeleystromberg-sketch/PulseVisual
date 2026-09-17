package dev.pulsevisual.gui.components;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, Supplier<Integer> getter, Consumer<Integer> setter) { super(name, getter, setter); }
}
