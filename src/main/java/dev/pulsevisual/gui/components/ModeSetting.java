package dev.pulsevisual.gui.components;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModeSetting extends Setting<String> {
    public final String[] options;
    public ModeSetting(String name, String[] options, Supplier<String> getter, Consumer<String> setter) {
        super(name, getter, setter); this.options = options;
    }
    public void next() {
        int i = 0;
        for (int n = 0; n < options.length; n++) if (options[n].equals(get())) i = n;
        set(options[(i + 1) % options.length]);
    }
}
