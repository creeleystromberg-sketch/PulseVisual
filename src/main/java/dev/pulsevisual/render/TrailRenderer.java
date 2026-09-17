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

public final class TrailRenderer {
    private static final int CAPACITY=160;
    private static final double[] X=new double[CAPACITY],Y=new double[CAPACITY],Z=new double[CAPACITY];
    private static final long[] TIME=new long[CAPACITY];
    private static int start,count;
    private static Object previousWorld;

    private TrailRenderer() { }
    public static void reset() { start=0; count=0; previousWorld=null; }
    private static int index(int n) { return (start+n)%CAPACITY; }
    public static void tick(Minecraft client) {
        VisualConfig.Trail c=ConfigManager.get().trail;
        if(!c.enabled || client.player==null || client.level==null) { reset(); return; }
        if(previousWorld!=client.level) { reset(); previousWorld=client.level; }
        long now=System.nanoTime();
        while(count>0 && (now-TIME[start])/1e9>c.length/c.fadeSpeed) { start=(start+1)%CAPACITY; count--; }
        if(c.sprintOnly&&!client.player.isSprinting() || c.groundOnly&&!client.player.onGround())return;
        Vec3 pos=client.player.position();
        if(count>0) {
            int last=index(count-1);
            double dist=(pos.x-X[last])*(pos.x-X[last])+(pos.z-Z[last])*(pos.z-Z[last])+(pos.y+c.height-Y[last])*(pos.y+c.height-Y[last]);
            if(dist>16) { reset(); previousWorld=client.level; }
            else if(dist<c.minDistance*c.minDistance) return;
        }
        while(count>=Math.min(CAPACITY,c.maxPoints)) { start=(start+1)%CAPACITY; count--; }
        int at=index(count++); X[at]=pos.x; Y[at]=pos.y+c.height; Z[at]=pos.z; TIME[at]=now;
    }
    public static void render(WorldRenderContext context) {
        VisualConfig.Trail c=ConfigManager.get().trail;
        if(!c.enabled || Minecraft.getInstance().options.getCameraType().isFirstPerson() || count<2 || context.consumers()==null) return;
        Vec3 camera=context.worldState().cameraRenderState.pos;
        PoseStack pose=context.matrices(); pose.pushPose(); pose.translate(-camera.x,-camera.y,-camera.z);
        Matrix4fc matrix=pose.last().pose();
        VertexConsumer quads=context.consumers().getBuffer(RenderTypes.debugQuads());
        long now=System.nanoTime();
        int subdivisions=1+Math.round(c.smoothness*4);
        double px=0,py=0,pz=0;
        boolean first=true;
        int segments=(count-1)*subdivisions;
        for(int s=0;s<=segments;s++) {
            int pair=Math.min(count-2,s/subdivisions);
            double t=s==segments?1:(s%subdivisions)/(double)subdivisions;
            int a=index(Math.max(0,pair-1)),b=index(pair),d=index(pair+1),e=index(Math.min(count-1,pair+2));
            double x=curve(X[a],X[b],X[d],X[e],t), y=curve(Y[a],Y[b],Y[d],Y[e],t);
            double z=curve(Z[a],Z[b],Z[d],Z[e],t);
            if(!first) {
                float age=(float)((now-TIME[b])/1e9);
                float fade=Math.max(0,1-age*c.fadeSpeed/c.length);
                fade*=Math.min(1,s/(float)Math.max(1,segments)*2);
                float alpha=c.alpha*fade;
                if(c.mode.equals("Pulse")) alpha*=.65f+.35f*(float)Math.sin(now/4e8*c.pulseSpeed-s*.2);
                int color=color(c,s/(float)Math.max(1,segments),now);
                double dx=x-px,dz=z-pz,len=Math.sqrt(dx*dx+dz*dz);
                if(len>.0001) {
                    double nx=-dz/len,nz=dx/len;
                    float taper=(float)Math.sin(Math.min(1,s/(float)Math.max(1,segments))*Math.PI*.5);
                    float width=c.width*(c.mode.equals("Line")?.15f:1f)*(1-c.taper+c.taper*taper);
                    // Feather the ribbon instead of drawing a wide, hard-edged slab.
                    ribbon(quads,matrix,px,py,pz,x,y,z,nx,nz,width,color,alpha*.22f);
                    ribbon(quads,matrix,px,py+.001,pz,x,y+.001,z,nx,nz,width*.64f,color,alpha*.5f);
                    if(c.mode.equals("Glow")) ribbon(quads,matrix,px,py+.003,pz,x,y+.003,z,nx,nz,width*c.glowWidth,color,alpha*.16f*c.glowStrength);
                }
            }
            px=x;py=y;pz=z;first=false;
        }
        pose.popPose();
    }
    private static void ribbon(VertexConsumer out,Matrix4fc m,double ax,double ay,double az,double bx,double by,double bz,
                               double nx,double nz,float width,int color,float alpha) {
        double half=width*.5;
        WorldDraw.quad(out,m,ax+nx*half,ay,az+nz*half,ax-nx*half,ay,az-nz*half,
                bx-nx*half,by,bz-nz*half,bx+nx*half,by,bz+nz*half,color,alpha);
    }
    private static double curve(double a,double b,double c,double d,double t) {
        return .5*((2*b)+(-a+c)*t+(2*a-5*b+4*c-d)*t*t+(-a+3*b-3*c+d)*t*t*t);
    }
    private static int color(VisualConfig.Trail c,float t,long now) {
        if(c.mode.equals("Rainbow")) return ColorUtil.rainbow((float)(now/1e9*c.rainbowSpeed*.12+t*.6));
        return c.mode.equals("Gradient") || c.gradient ? ColorUtil.mix(c.color,c.secondaryColor,t) : c.color;
    }
}
