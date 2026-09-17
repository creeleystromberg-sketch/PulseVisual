package dev.pulsevisual.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;

/** Render-only state: never calls swing(), modifies cooldown, or sends packets. */
public final class SwingAnimator {
    public static boolean continuousMining;
    private static final VisualSwingClock[] CLOCKS={new VisualSwingClock(),new VisualSwingClock()};
    private static final Profile[] ACTIVE=new Profile[2];
    private static final Quaternionf ROTATION=new Quaternionf();
    private static Object player;
    private static String lastMode;
    private static boolean lastEnabled;
    private static void syncSettings(){
        var c=ConfigManager.get().swing;
        if(!java.util.Objects.equals(lastMode,c.preset)||lastEnabled!=c.enabled){
            for(var clock:CLOCKS)clock.cancel();
            lastMode=c.preset;lastEnabled=c.enabled;
        }
    }
    private SwingAnimator() { }
    private record Profile(float duration,float strength,float rotation,float x,float y,float z,float smoothness,String easing,float rotationX,float rotationY,float peak) {
        Profile(float d,float s,float r,float x,float y,float z,float smooth,String ease){this(d,s,r,x,y,z,smooth,ease,-r*.25f,r*.25f,.36f);}
    }
    private static Profile profile(VisualConfig.Swing c) {
        return switch(c.preset) {
            case "Slow" -> new Profile(.78f,.8f,18,-.025f,-.01f,0,1,"Sine");
            case "Fast" -> new Profile(.23f,.85f,16,-.02f,0,0,1,"Sine");
            case "Sharp" -> new Profile(.30f,1,26,-.04f,-.02f,0,.65f,"Cubic");
            case "Soft" -> new Profile(.62f,.6f,14,-.015f,0,0,1,"Sine");
            case "Swipe" -> new Profile(.44f,1,38,-.09f,0,-.02f,.85f,"Quint");
            case "Custom" -> new Profile(c.duration/c.speed,c.strength,c.rotation,c.offsetX,c.offsetY,c.offsetZ,c.smoothness,c.easing,c.rotationX,c.rotationY,c.peak);
            default -> new Profile(.48f,.8f,18,-.025f,0,0,1,"Sine");
        };
    }
    public static void reset() {
        for(int i=0;i<2;i++){ CLOCKS[i].reset(); ACTIVE[i]=null; }
        player=null;
    }
    public static void tick(Minecraft client) {
        if(client.player!=player){reset();player=client.player;}
    }
    public static void onSwing(InteractionHand hand,int eventTick) {
        if(continuousMining)return;
        syncSettings();
        VisualConfig.Swing c=ConfigManager.get().swing;
        if(!c.enabled || "Vanilla".equals(c.preset))return;
        int i=hand.ordinal(); Profile p=profile(c);
        if(CLOCKS[i].start(eventTick,System.nanoTime(),p.duration)) ACTIVE[i]=p;
    }
    public static float progress(float vanilla,InteractionHand hand) {
        syncSettings();
        VisualConfig.Swing c=ConfigManager.get().swing;
        return !c.enabled || "Vanilla".equals(c.preset) ? vanilla : 0;
    }
    public static void apply(PoseStack pose,InteractionHand hand,float vanilla) {
        syncSettings();
        VisualConfig.Swing c=ConfigManager.get().swing;
        if(!c.enabled || "Vanilla".equals(c.preset))return;
        int i=hand.ordinal(); Profile p=ACTIVE[i];
        float t=CLOCKS[i].progress(System.nanoTime());
        if(p==null || t<0)return;
        float wave=VisualSwingClock.envelope(t,p.smoothness,p.easing,p.peak)*p.strength;
        var client=Minecraft.getInstance();
        boolean right=client.player==null || client.player.getMainArm()==HumanoidArm.RIGHT;
        float side=(hand==InteractionHand.MAIN_HAND)==right?1:-1;
        pose.translate(p.x*wave*side,p.y*wave,p.z*wave);
        pose.mulPose(ROTATION.rotationXYZ((float)Math.toRadians(p.rotationX*wave),
                (float)Math.toRadians(p.rotationY*side*wave),(float)Math.toRadians(p.rotation*side*wave)));
    }
}
