package dev.pulsevisual.animation;

/** One accepted vanilla event starts one finite interval; sampling is read-only. */
public final class VisualSwingClock {
    private int lastEvent=Integer.MIN_VALUE;
    private long started;
    private double duration;
    private boolean active;
    public boolean start(int event,long now,float seconds) {
        if(event==lastEvent)return false;
        lastEvent=event; started=now; duration=Math.max(.05,seconds)*1e9; active=true;
        return true;
    }
    public float progress(long now) {
        if(!active)return -1;
        double t=(now-started)/duration;
        return t>=1?-1:(float)Math.max(0,t);
    }
    public void reset(){ active=false; lastEvent=Integer.MIN_VALUE; }
    public void cancel(){active=false;}
    public static float envelope(float t,float smoothness,String easing) {
        return envelope(t,smoothness,easing,.36f);
    }
    public static float envelope(float t,float smoothness,String easing,float peak) {
        float u=t<peak?t/peak:(1-t)/(1-peak);
        u=Math.max(0,Math.min(1,u));
        float eased=switch(easing) {
            case "Linear" -> u;
            case "Sine" -> (float)(.5-.5*Math.cos(Math.PI*u));
            case "Quint" -> u*u*u*(u*(u*6-15)+10);
            default -> u*u*(3-2*u);
        };
        return u+(eased-u)*smoothness;
    }
}
