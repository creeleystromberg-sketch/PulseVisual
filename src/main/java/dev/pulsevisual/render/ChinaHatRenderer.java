package dev.pulsevisual.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import dev.pulsevisual.util.ColorUtil;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class ChinaHatRenderer {
    private static final int SLICES=32;
    private ChinaHatRenderer() { }

    public static void render(WorldRenderContext context) {
        Minecraft client=Minecraft.getInstance();
        VisualConfig.Hat cfg=ConfigManager.get().hat;
        if(!cfg.enabled || client.player==null || context.consumers()==null
                || client.options.getCameraType().isFirstPerson()) return;
        float partial=client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 pos=client.player.getPosition(partial);
        Vec3 camera=context.worldState().cameraRenderState.pos;
        double y=pos.y+client.player.getBbHeight()+cfg.height;
        double radius=cfg.size*.55;
        double peak=y+cfg.size*.3;
        double phase=System.nanoTime()/1_000_000_000d*cfg.rotationSpeed;
        float rainbowPhase=(float)(System.nanoTime()/1_000_000_000d*cfg.rainbowSpeed*.12);
        PoseStack pose=context.matrices(); pose.pushPose(); pose.translate(-camera.x,-camera.y,-camera.z);
        Matrix4fc matrix=pose.last().pose();
        boolean filled=!cfg.style.equals("Outline");
        boolean outlined=!cfg.style.equals("Filled");
        if(filled) {
            VertexConsumer out=context.consumers().getBuffer(RenderTypes.debugQuads());
            for(int i=0;i<SLICES;i++) {
                double a=phase+i*Math.PI*2/SLICES;
                double b=phase+(i+1)*Math.PI*2/SLICES;
                int color=color(cfg,i/(float)SLICES,rainbowPhase);
                float alpha=cfg.alpha*(cfg.gradient ? .55f+.45f*i/SLICES : 1);
                WorldDraw.quad(out,matrix,pos.x,peak,pos.z,
                        pos.x+Math.cos(a)*radius,y,pos.z+Math.sin(a)*radius,
                        pos.x+Math.cos(b)*radius,y,pos.z+Math.sin(b)*radius,
                        pos.x+Math.cos(b)*radius,y,pos.z+Math.sin(b)*radius,color,alpha);
            }
        }
        if(outlined) {
            VertexConsumer lines=context.consumers().getBuffer(RenderTypes.linesTranslucent());
            for(int i=0;i<SLICES;i++) {
                double a=phase+i*Math.PI*2/SLICES,b=phase+(i+1)*Math.PI*2/SLICES;
                int color=color(cfg,i/(float)SLICES,rainbowPhase);
                double x1=pos.x+Math.cos(a)*radius,z1=pos.z+Math.sin(a)*radius;
                double x2=pos.x+Math.cos(b)*radius,z2=pos.z+Math.sin(b)*radius;
                WorldDraw.line(lines,matrix,x1,y,z1,x2,y,z2,color,cfg.alpha);
                if(i%4==0) WorldDraw.line(lines,matrix,pos.x,peak,pos.z,x1,y,z1,color,cfg.alpha*.75f);
            }
        }
        pose.popPose();
    }
    private static int color(VisualConfig.Hat cfg,float index,float rainbowPhase) {
        int base=cfg.rainbow ? ColorUtil.rainbow(rainbowPhase+index) : cfg.color;
        return cfg.gradient ? ColorUtil.mix(base,0xFFFFFFFF,index*.28f) : base;
    }
}
