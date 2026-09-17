package dev.pulsevisual;
import dev.pulsevisual.animation.VisualSwingClock;
import dev.pulsevisual.config.VisualConfig;

/** Dependency-free regression checks; run with gradle visualRegression. */
public final class VisualRegressionTest {
    private static int checks;
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
    public static void main(String[] args){
        String[] modes={"Fast","Slow","Smooth","Sharp","Soft","Swipe","Custom"};
        float[] durations={.23f,.78f,.48f,.30f,.62f,.44f,.42f};
        for(int i=0;i<modes.length;i++){
            VisualSwingClock clock=new VisualSwingClock();
            long start=1_000_000_000L;
            check(clock.start(40,start,durations[i]),modes[i]+" single click starts");
            for(int frame=0;frame<240;frame++){
                long now=start+frame*8_333_333L;
                check(!clock.start(40,now,durations[i]),modes[i]+" same event must never restart");
                float progress=clock.progress(now);
                check(progress<0 || progress>=0&&progress<1,"bounded progress");
            }
            check(clock.progress(start+2_000_000_000L)<0,modes[i]+" ends without looping");
            for(int event=41;event<100;event++){
                long now=start+(event-40)*150_000_000L;
                check(clock.start(event,now,durations[i]),"held attack: each accepted vanilla swing starts once");
                check(!clock.start(event,now+1,durations[i]),"held attack duplicate rejected");
            }
            check(clock.progress(start+20_000_000_000L)<0,"release stops");
            clock.cancel();check(clock.progress(start)<0,"mode switch cancels");
            check(!clock.start(99,start,durations[i]),"switch does not reopen same event");
        }
        for(String easing:new String[]{"Linear","Sine","Cubic","Smoothstep","Quint"}){
            check(Math.abs(VisualSwingClock.envelope(0,1,easing))<1e-6,"pose starts at rest");
            check(Math.abs(VisualSwingClock.envelope(1,1,easing))<1e-6,"pose ends at rest");
            float max=0;
            for(int f=0;f<=1000;f++){float v=VisualSwingClock.envelope(f/1000f,1,easing);check(v>=0&&v<=1.00001,"no overshoot");max=Math.max(max,v);}
            check(max>.999,"one full tilt");
        }
        VisualConfig cfg=new VisualConfig();
        cfg.swing.preset="Fast";cfg.sanitize();check(cfg.swing.preset.equals("Vanilla"),"existing custom animations migrate to ordinary swing");
        cfg.swing.preset="Custom";cfg.sanitize();check(cfg.swing.preset.equals("Custom"),"explicit customization survives saving");
        for(float peak:new float[]{.15f,.36f,.8f}){
            check(VisualSwingClock.envelope(0,1,"Sine",peak)==0,"custom timing starts at rest");
            check(VisualSwingClock.envelope(1,1,"Sine",peak)==0,"custom timing returns to rest");
            check(VisualSwingClock.envelope(peak,1,"Sine",peak)>.999f,"custom timing reaches its selected peak");
        }
        for(float fire:new float[]{0,10,25,50,100}){cfg.fire.size=fire;cfg.sanitize();check(cfg.fire.size==fire,"fire percent preserved");}
        cfg.viewModel.right.x=100;cfg.viewModel.left.scale=-10;cfg.theme.guiScale=99;cfg.pearl.length=999;
        cfg.sanitize();check(cfg.viewModel.right.x==.4f,"safe position range");check(cfg.viewModel.left.scale==.65f,"safe scale");
        check(cfg.theme.guiScale==1.5f,"menu scale bounds");check(cfg.pearl.length==8,"bounded trail age");
        System.out.println("PASS: "+checks+" regression checks (timer, event deduplication, mode switches, easing, config bounds)");
    }
}
