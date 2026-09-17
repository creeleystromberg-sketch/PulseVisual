package dev.pulsevisual.gui.components;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class NumberSetting extends Setting<Float> {
    public final float min, max;
    public final boolean integer;
    public float resetValue;
    public NumberSetting(String name, float min, float max, boolean integer, Supplier<Float> getter, Consumer<Float> setter) {
        super(name, getter, setter); this.min = min; this.max = max; this.integer = integer;
        this.resetValue=Math.max(min,Math.min(max,0));
    }
    public NumberSetting resetTo(float value){resetValue=value;return this;}
    @Override public void preview(Float value){
        if(Float.isFinite(value))super.preview(Math.max(min,Math.min(max,integer?(float)Math.round(value):value)));
    }
    public float fraction() { return Math.max(0, Math.min(1, (get() - min) / (max - min))); }
    public void fraction(float value) {
        float result = min + Math.max(0, Math.min(1, value)) * (max - min);
        set(integer ? (float) Math.round(result) : result);
    }
}
