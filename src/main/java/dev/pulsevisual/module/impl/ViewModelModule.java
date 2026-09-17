package dev.pulsevisual.module.impl;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import java.util.function.Function;

public final class ViewModelModule extends Module {
    public ViewModelModule(){
        super("view_model","View Model / live preview","View Model");
        add(new BooleanSetting("Enable View Model",()->c().enabled,v->c().enabled=v));
        add(new ModeSetting("Edit hand",new String[]{"Both Hands","Main Hand","Off Hand","Right Hand","Left Hand"},()->c().target,v->c().target=v));
        add(new ModeSetting("Preset",new String[]{"Vanilla","Compact","Low","Centered","PvP","Custom"},()->c().preset,ViewModelModule::preset));
        slider("Position X",-.4f,.4f,0,h->h.x,(h,v)->h.x=v);
        slider("Position Y",-.35f,.35f,0,h->h.y,(h,v)->h.y=v);
        slider("Position Z",-.4f,.25f,0,h->h.z,(h,v)->h.z=v);
        slider("Rotation X",-45,45,0,h->h.rotX,(h,v)->h.rotX=v);
        slider("Rotation Y",-45,45,0,h->h.rotY,(h,v)->h.rotY=v);
        slider("Rotation Z",-45,45,0,h->h.rotZ,(h,v)->h.rotZ=v);
        slider("Scale",.65f,1.3f,1,h->h.scale,(h,v)->h.scale=v);
        add(new ActionSetting("Mirror Settings",()->{
            boolean right=right();copy(selected(),right?c().left:c().right,true);c().preset="Custom";
        }));
        add(new ActionSetting("Reset View Model",()->ConfigManager.get().viewModel=new VisualConfig.ViewModel()));
    }
    private static VisualConfig.ViewModel c(){return ConfigManager.get().viewModel;}
    private static boolean right(){
        var p=Minecraft.getInstance().player;
        boolean mainRight=p==null || p.getMainArm()==HumanoidArm.RIGHT;
        return switch(c().target){case "Left Hand"->false;case "Main Hand"->mainRight;case "Off Hand"->!mainRight;default->true;};
    }
    private static VisualConfig.Hand selected(){return right()?c().right:c().left;}
    private static void copy(VisualConfig.Hand a,VisualConfig.Hand b,boolean mirror){
        b.x=mirror?-a.x:a.x;b.y=a.y;b.z=a.z;b.rotX=a.rotX;b.rotY=mirror?-a.rotY:a.rotY;b.rotZ=mirror?-a.rotZ:a.rotZ;b.scale=a.scale;
    }
    private static void preset(String name){
        c().preset=name;if(name.equals("Custom"))return;
        VisualConfig.Hand h=new VisualConfig.Hand();
        switch(name){
            case "Compact"->{h.scale=.8f;h.y=-.04f;h.z=-.12f;}
            case "Low"->{h.y=-.18f;h.scale=.95f;}
            case "Centered"->{h.x=-.20f;h.rotY=-10;}
            case "PvP"->{h.x=-.07f;h.y=-.12f;h.scale=.85f;h.rotZ=-6;}
        }
        if(c().target.equals("Both Hands")){copy(h,c().right,false);copy(h,c().left,true);}
        else copy(h,selected(),!right());
    }
    private interface Setter {void set(VisualConfig.Hand h,float v);}
    private void slider(String name,float min,float max,float def,Function<VisualConfig.Hand,Float> get,Setter set){
        add(new NumberSetting(name,min,max,false,()->get.apply(selected()),v->{
            set.set(selected(),v);
            if(c().target.equals("Both Hands"))set.set(c().left,
                    name.equals("Position X") || name.equals("Rotation Y") || name.equals("Rotation Z") ? -v : v);
            c().preset="Custom";
        }).resetTo(def));
    }
}
