package dev.pulsevisual.test;

import dev.pulsevisual.animation.SwingAnimator;
import dev.pulsevisual.animation.VisualSwingClock;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.gui.SettingsScreen;
import dev.pulsevisual.render.PearlTracer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public final class VisualGameTest implements FabricClientGameTest {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static Object field(Object owner,Class<?> type,String name){
        try{Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(owner);}catch(Exception e){throw new AssertionError(e);}
    }
    private static void field(Object owner,Class<?> type,String name,Object value){
        try{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(owner,value);}catch(Exception e){throw new AssertionError(e);}
    }
    private static int geometry(SettingsScreen s,String method){
        try{Method m=SettingsScreen.class.getDeclaredMethod(method);m.setAccessible(true);return (int)m.invoke(s);}catch(Exception e){throw new AssertionError(e);}
    }
    private static VisualSwingClock clock(){return ((VisualSwingClock[])field(null,SwingAnimator.class,"CLOCKS"))[0];}
    private static long started(){return (long)field(clock(),VisualSwingClock.class,"started");}
    private static int event(){return (int)field(clock(),VisualSwingClock.class,"lastEvent");}
    private static Map<?,?> pearls(){return (Map<?,?>)field(null,PearlTracer.class,"TRAILS");}
    private static void click(SettingsScreen s,double x,double y){
        float scale=(float)field(s,SettingsScreen.class,"scale");
        MouseButtonEvent e=new MouseButtonEvent(x*scale,y*scale,new MouseButtonInfo(0,0));
        s.mouseClicked(e,false);s.mouseReleased(e);
    }
    @Override public void runTest(ClientGameTestContext ctx){
        ctx.getInput().resizeWindow(1280,800);
        ctx.runOnClient(mc->ConfigManager.resetAll());
        try(var world=ctx.worldBuilder().create()){
            world.getClientWorld().waitForChunksRender();
            world.getServer().runCommand("item replace entity @p weapon.mainhand with minecraft:diamond_sword");
            ctx.runOnClient(mc->{mc.player.setXRot(-15);mc.player.setYRot(0);ConfigManager.get().weaponEffects=false;ConfigManager.get().swing.enabled=true;});
            ctx.waitTicks(12);
            check(ctx.computeOnClient(mc->ConfigManager.get().swing.preset.equals("Vanilla")),"ordinary Minecraft swing is the default");
            for(String mode:new String[]{"Fast","Slow","Smooth","Sharp"}){
                ctx.runOnClient(mc->{ConfigManager.get().swing.preset=mode;SwingAnimator.reset();});
                ctx.waitTicks(2);
                ctx.getInput().pressKey(o->o.keyAttack);
                for(int equipTick=0;equipTick<5;equipTick++){
                    ctx.waitTick();
                    check(ctx.computeOnClient(mc->(float)field(mc.gameRenderer.itemInHandRenderer,net.minecraft.client.renderer.ItemInHandRenderer.class,"mainHandHeight"))>.95f,mode+" attack must not lower equipped sword");
                }
                long first=ctx.computeOnClient(mc->started());
                check(ctx.computeOnClient(mc->event())>=0,mode+" input starts a visual swing");
                ctx.waitTicks(25);
                long rest=ctx.computeOnClient(mc->started());
                check(first==rest,mode+" one click is exactly one animation");
                ctx.waitTicks(20);
                check(rest==ctx.computeOnClient(mc->started()),mode+" idle must not restart");
                ctx.getInput().holdKey(o->o.keyAttack);
                int changes=0,last=ctx.computeOnClient(mc->event());
                for(int i=0;i<30;i++){
                    ctx.waitTick();int current=ctx.computeOnClient(mc->event());
                    if(current!=last){changes++;last=current;}
                }
                ctx.getInput().releaseKey(o->o.keyAttack);
                check(changes<=10,mode+" held key does not restart every tick");
                ctx.waitTicks(3);long released=ctx.computeOnClient(mc->started());
                ctx.waitTicks(20);check(released==ctx.computeOnClient(mc->started()),mode+" no auto swing after release");
                ctx.runOnClient(mc->mc.player.setXRot(80));
                ctx.waitTicks(5);ctx.getInput().holdKey(o->o.keyAttack);
                int miningChanges=0,miningLast=ctx.computeOnClient(mc->event());
                for(int i=0;i<30;i++){ctx.waitTick();int current=ctx.computeOnClient(mc->event());if(current!=miningLast){miningChanges++;miningLast=current;}}
                ctx.getInput().releaseKey(o->o.keyAttack);
                check(miningChanges<=1,mode+" held block attack must not retrigger visual swing");
                ctx.runOnClient(mc->mc.player.setXRot(-15));ctx.waitTicks(20);
                ctx.runOnClient(mc->{SwingAnimator.onSwing(net.minecraft.world.InteractionHand.MAIN_HAND,mc.player.tickCount);field(clock(),VisualSwingClock.class,"started",System.nanoTime()-100_000_000L);});
                ctx.takeScreenshot("swing-"+mode.toLowerCase());
                System.out.println("[PulseQA] PASS swing "+mode+"; held starts="+changes);
            }
            world.getServer().runCommand("tp @p 3 -60 -2 0 -15");ctx.waitTicks(5);
            ctx.runOnClient(mc->{
                for(String mode:new String[]{"Fast","Slow","Smooth","Sharp","Vanilla","Fast","Custom","Slow"}){
                    ConfigManager.get().swing.preset=mode;SwingAnimator.progress(0,net.minecraft.world.InteractionHand.MAIN_HAND);
                    check(clock().progress(System.nanoTime())<0,"mode switch never starts a swing");
                }
                ConfigManager.get().swing.preset="Smooth";
            });
            for(String item:new String[]{"diamond_sword","diamond_axe","diamond_pickaxe","stone","apple","bow","air"}){
                world.getServer().runCommand("item replace entity @p weapon.mainhand with minecraft:"+item);
                ctx.runOnClient(mc->{var vm=ConfigManager.get().viewModel;vm.right.x=-.10f;vm.right.y=-.08f;vm.right.rotZ=-6;vm.right.scale=.9f;});
                ctx.waitTicks(35);ctx.takeScreenshot("viewmodel-"+item);
            }
            world.getServer().runCommand("item replace entity @p weapon.mainhand with minecraft:diamond_sword");
            world.getServer().runCommand("item replace entity @p weapon.offhand with minecraft:diamond_sword");
            ctx.runOnClient(mc->{
                ConfigManager.get().viewModel=new dev.pulsevisual.config.VisualConfig.ViewModel();
                var module=new dev.pulsevisual.module.impl.ViewModelModule();
                for(var setting:module.settings())if(setting.name().equals("Position X"))((dev.pulsevisual.gui.components.NumberSetting)setting).preview(-.12f);
                check(ConfigManager.get().viewModel.left.x==.12f,"Both Hands mirrors horizontal offset");
                ConfigManager.get().viewModel.right.x=0;ConfigManager.get().viewModel.left.x=0;
                ConfigManager.get().swing.preset="Custom";
                mc.player.resetAttackStrengthTicker();
                check(mc.player.getAttackStrengthScale(0)<.1f,"visual swing leaves actual attack cooldown unchanged");
            });
            ctx.waitTicks(30);
            for(var arm:net.minecraft.world.entity.HumanoidArm.values()){
                ctx.runOnClient(mc->{mc.player.setMainArm(arm);SwingAnimator.reset();});ctx.waitTicks(2);
                ctx.takeScreenshot("hands-"+arm+"-rest");
                for(var hand:net.minecraft.world.InteractionHand.values()){
                    ctx.runOnClient(mc->{SwingAnimator.onSwing(hand,mc.player.tickCount);var clocks=(VisualSwingClock[])field(null,SwingAnimator.class,"CLOCKS");field(clocks[hand.ordinal()],VisualSwingClock.class,"started",System.nanoTime()-140_000_000L);});
                    ctx.takeScreenshot("hands-"+arm+"-"+hand+"-tilt");ctx.waitTicks(15);
                }
            }
            ctx.runOnClient(mc->mc.player.setMainArm(net.minecraft.world.entity.HumanoidArm.RIGHT));
            world.getServer().runCommand("item replace entity @p weapon.offhand with minecraft:air");
            for(float size:new float[]{.5f,1,2}){
                ctx.runOnClient(mc->{ConfigManager.get().customizeCrosshair=true;ConfigManager.get().crosshairSize=size;});
                ctx.waitTick();ctx.takeScreenshot("vanilla-crosshair-"+size);
            }
            ctx.runOnClient(mc->ConfigManager.get().customizeCrosshair=false);
            ctx.runOnClient(mc->{mc.player.setXRot(0);mc.player.setYRot(0);});
            world.getServer().runCommand("effect give @p minecraft:fire_resistance 120 0 true");
            ctx.waitTicks(30);
            for(float size:new float[]{0,10,25,50,100}){
                world.getServer().runOnServer(server->server.getPlayerList().getPlayers().getFirst().setRemainingFireTicks(200));
                ctx.runOnClient(mc->{ConfigManager.get().fire.size=size;mc.player.setSharedFlagOnFire(true);});
                ctx.waitTicks(2);ctx.takeScreenshot("fire-"+(int)size);
                check(ctx.computeOnClient(mc->mc.player.isOnFire()),"fire size must not clear real burning");
            }
            world.getServer().runOnServer(server->server.getPlayerList().getPlayers().getFirst().clearFire());
            ctx.runOnClient(mc->{mc.player.clearFire();mc.player.setSharedFlagOnFire(false);ConfigManager.get().fire.size=100;});
            // UI checks at multiple menu scales, including actual slider event coordinates.
            float previousScale=0;
            for(float menuScale:new float[]{.65f,1,1.5f}){
                ctx.runOnClient(mc->{ConfigManager.get().theme.guiScale=menuScale;ConfigManager.get().theme.animations=false;mc.setScreen(new SettingsScreen(null));});
                ctx.waitTicks(2);
                float actualScale=ctx.computeOnClient(mc->(float)field(mc.screen,SettingsScreen.class,"scale"));
                check(actualScale>previousScale,"menu scale visibly changes at each setting");previousScale=actualScale;
                ctx.runOnClient(mc->{
                    SettingsScreen s=(SettingsScreen)mc.screen;
                    click(s,geometry(s,"x")+245,geometry(s,"y")+55); // Render tab
                });
                ctx.waitTick();
                ctx.runOnClient(mc->{
                    SettingsScreen s=(SettingsScreen)mc.screen;
                    int row=geometry(s,"top")+3*47;
                    click(s,geometry(s,"sx")+geometry(s,"sw")*.7,row+31);
                    check(Math.abs(ConfigManager.get().viewModel.right.x-.16f)<.025,"scaled slider hit target");
                });
                ctx.takeScreenshot("menu-scale-"+menuScale);
                ctx.runOnClient(mc->{
                    SettingsScreen s=(SettingsScreen)mc.screen;
                    click(s,geometry(s,"cx")+geometry(s,"cw")-80,geometry(s,"top")+3*47+31);
                });
                ctx.getInput().holdControl();ctx.getInput().pressKey(org.lwjgl.glfw.GLFW.GLFW_KEY_A);ctx.getInput().releaseControl();
                ctx.getInput().typeChars("0.123");
                ctx.takeScreenshot("number-editor-"+menuScale);
                System.out.println("[PulseQA] editor value="+ctx.computeOnClient(mc->{var e=(net.minecraft.client.gui.components.EditBox)field(mc.screen,SettingsScreen.class,"editor");return e==null?"NULL":e.getValue();}));
                ctx.getInput().pressKey(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER);
                check(ctx.computeOnClient(mc->Math.abs(ConfigManager.get().viewModel.right.x-.123f)<.001f),"exact number entry: "+ctx.computeOnClient(mc->ConfigManager.get().viewModel.right.x));
                ctx.runOnClient(mc->{
                    SettingsScreen s=(SettingsScreen)mc.screen;
                    click(s,geometry(s,"cx")+geometry(s,"cw")-20,geometry(s,"top")+3*47+31);
                    check(ConfigManager.get().viewModel.right.x==0,"per-parameter reset");
                });
                ctx.setScreen(()->null);
            }
            ctx.runOnClient(mc->{ConfigManager.get().pearl.enabled=true;ConfigManager.get().pearl.scope="Own Pearls";mc.player.setXRot(-65);});
            world.getServer().runCommand("item replace entity @p weapon.mainhand with minecraft:ender_pearl 16");ctx.waitTicks(10);
            ctx.getInput().pressKey(o->o.keyUse);ctx.waitTicks(5);
            check(ctx.computeOnClient(mc->!pearls().isEmpty()),"own pearl owner resolves from server");
            ctx.runOnClient(mc->mc.player.setYRot(18));
            ctx.takeScreenshot("pearl-own");
            ctx.waitTicks(18);ctx.getInput().pressKey(o->o.keyUse);ctx.waitTicks(8);
            ctx.runOnClient(mc->{mc.player.setYRot(35);mc.player.setXRot(-35);});
            check(ctx.computeOnClient(mc->pearls().size()>=2),"simultaneous pearls have separate trails");
            ctx.takeScreenshot("pearl-multiple");
            world.getServer().runCommand("summon minecraft:ender_pearl ~5 ~10 ~5 {NoGravity:1b,Motion:[0.0d,0.0d,0.05d]}");ctx.waitTicks(2);
            int own=ctx.computeOnClient(mc->pearls().size());
            ctx.runOnClient(mc->ConfigManager.get().pearl.scope="All Pearls");ctx.waitTicks(2);
            check(ctx.computeOnClient(mc->pearls().size())==own,"legacy All setting cannot include unknown/foreign pearls");
            world.getServer().runCommand("kill @e[type=minecraft:ender_pearl]");ctx.waitTicks(3);
            check(ctx.computeOnClient(mc->pearls().isEmpty()),"removed pearls are cleaned up");
            ctx.runOnClient(mc->{ConfigManager.get().trail.enabled=true;mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);mc.player.setXRot(20);});
            ctx.getInput().holdKeyFor(o->o.keyUp,15);ctx.takeScreenshot("trail-third-person");
            ctx.runOnClient(mc->mc.options.setCameraType(CameraType.FIRST_PERSON));ctx.takeScreenshot("trail-first-person-hidden");
            world.getServer().runCommand("summon minecraft:zombie ~ ~ ~3 {NoAI:1b,Silent:1b,Invulnerable:1b}");ctx.waitTicks(3);
            for(String shape:new String[]{"Burst","Ring","Upward"}){
                ctx.runOnClient(mc->{
                    ConfigManager.get().hit.shape=shape;ConfigManager.get().hit.particle="End Rod";
                    for(var entity:mc.level.entitiesForRendering())if(entity instanceof net.minecraft.world.entity.monster.zombie.Zombie){dev.pulsevisual.effect.HitEffectManager.onAttack(entity,true);break;}
                    check(dev.pulsevisual.effect.HitEffectManager.flashProgress()>0,"hit effect "+shape+" activates");
                });
                ctx.waitTicks(2);ctx.takeScreenshot("hit-"+shape);ctx.waitTicks(20);
            }
            ctx.runOnClient(mc->{ConfigManager.get().swing.preset="Custom";mc.setScreen(new SettingsScreen(null));field(mc.screen,SettingsScreen.class,"category",2);});
            ctx.waitTicks(2);ctx.takeScreenshot("custom-swing-settings");ctx.setScreen(()->null);
            ctx.runOnClient(mc->{ConfigManager.get().theme.guiScale=1;ConfigManager.get().theme.font="Uniform";ConfigManager.get().theme.style="Minimal";mc.setScreen(new SettingsScreen(null));});
            ctx.waitTicks(3);ctx.takeScreenshot("menu-uniform-minimal");ctx.setScreen(()->null);
        }
        System.out.println("[PulseQA] PASS "+checks+" in-game assertions; screenshots recorded");
    }
}
