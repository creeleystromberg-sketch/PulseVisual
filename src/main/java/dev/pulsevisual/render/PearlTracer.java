package dev.pulsevisual.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import dev.pulsevisual.util.ColorUtil;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import java.util.HashMap;
import java.util.Map;

/** Bounded per-projectile trails. No prediction, projectile mutation or network traffic. */
public final class PearlTracer {
    private static final int POINTS=160,MAX_PEARLS=64;
    private static final Map<Integer,Trail> TRAILS=new HashMap<>();
    private static Object world;
    private static Vec3 lastPlayer;
    private static int tick;
    private static final class Trail {
        final ThrownEnderpearl entity;
        final double[] x=new double[POINTS],y=new double[POINTS],z=new double[POINTS];
        final long[] time=new long[POINTS];
        int start,count;
        double headX,headY,headZ;
        Trail(ThrownEnderpearl entity){this.entity=entity;}
        int index(int n){return(start+n)%POINTS;}
        void prune(long now,float seconds){while(count>0 && (now-time[start])/1e9>seconds){start=(start+1)%POINTS;count--;}}
        void add(Vec3 p,long now){
            if(count==POINTS){start=(start+1)%POINTS;count--;}
            int i=index(count++);x[i]=p.x;y[i]=p.y;z[i]=p.z;time[i]=now;
        }
        double coord(double[] values,int n,int axis){
            if(n==count-1)return axis==0?headX:axis==1?headY:headZ;
            return values[index(Math.max(0,Math.min(count-1,n)))];
        }
        double curve(double[] v,int n,double t,int axis){
            double a=coord(v,n-1,axis),b=coord(v,n,axis),c=coord(v,n+1,axis),d=coord(v,Math.min(count-1,n+2),axis);
            return .5*(2*b+(-a+c)*t+(2*a-5*b+4*c-d)*t*t+(-a+3*b-3*c+d)*t*t*t);
        }
    }
    public static void reset(){TRAILS.clear();world=null;lastPlayer=null;tick=0;}
    public static void clearAfterTeleport(){if(ConfigManager.get().pearl.clearOnTeleport)TRAILS.clear();lastPlayer=null;}
    private static boolean allowed(ThrownEnderpearl pearl,Minecraft client,VisualConfig.Pearl c){
        // Use the server-provided owner. Unknown ownership never falls back to
        // proximity/throw timing, which could accidentally claim somebody else's pearl.
        return client.player!=null && pearl.getOwner()==client.player;
    }
    public static void tick(Minecraft client){
        var c=ConfigManager.get().pearl;
        if(!c.enabled || client.level==null || client.player==null){reset();return;}
        if(world!=client.level){reset();world=client.level;}
        Vec3 p=client.player.position();
        if(lastPlayer!=null && p.distanceToSqr(lastPlayer)>64 && c.clearOnTeleport)TRAILS.clear();
        lastPlayer=p;long now=System.nanoTime();tick++;
        TRAILS.values().removeIf(t->t.entity.isRemoved() || client.level.getEntity(t.entity.getId())!=t.entity || !allowed(t.entity,client,c));
        for(Trail t:TRAILS.values())t.prune(now,c.length);
        int stride=TRAILS.size()>32?3:TRAILS.size()>16?2:1;
        for(Entity e:client.level.entitiesForRendering()){
            if(!(e instanceof ThrownEnderpearl pearl) || !allowed(pearl,client,c) || pearl.isRemoved())continue;
            Trail trail=TRAILS.get(pearl.getId());
            if(trail==null){
                if(TRAILS.size()>=MAX_PEARLS)continue;
                trail=new Trail(pearl);TRAILS.put(pearl.getId(),trail);
                // Spawn packets may not have initialized previous-tick coordinates yet.
                if(pearl.tickCount>0 && pearl.position().distanceToSqr(pearl.xo,pearl.yo,pearl.zo)<9)
                    trail.add(new Vec3(pearl.xo,pearl.yo,pearl.zo),now-50_000_000L);
            }
            if(tick%stride==0 || trail.count==0)trail.add(pearl.position(),now);
        }
    }
    public static void render(WorldRenderContext context){
        var c=ConfigManager.get().pearl;
        if(!c.enabled || TRAILS.isEmpty() || context.consumers()==null)return;
        Minecraft client=Minecraft.getInstance();
        Vec3 camera=context.worldState().cameraRenderState.pos;
        var pose=context.matrices();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
        Matrix4fc matrix=pose.last().pose();
        VertexConsumer out=context.consumers().getBuffer(RenderTypes.debugQuads());
        float partial=client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        long now=System.nanoTime();
        int subdivisions="Line".equals(c.mode)?1:TRAILS.size()>32?1:TRAILS.size()>16?2:4;
        for(Trail t:TRAILS.values()){
            if(t.count<2 || t.entity.isRemoved() || !allowed(t.entity,client,c))continue;
            t.headX=t.entity.xo+(t.entity.getX()-t.entity.xo)*partial;
            t.headY=t.entity.yo+(t.entity.getY()-t.entity.yo)*partial;
            t.headZ=t.entity.zo+(t.entity.getZ()-t.entity.zo)*partial;
            double px=t.x[t.start],py=t.y[t.start],pz=t.z[t.start];
            int segments=(t.count-1)*subdivisions;
            for(int s=1;s<=segments;s++){
                int pair=(s-1)/subdivisions;
                double u=((s-1)%subdivisions+1)/(double)subdivisions;
                double x=t.curve(t.x,pair,u,0),y=t.curve(t.y,pair,u,1),z=t.curve(t.z,pair,u,2);
                double stamp=t.time[t.index(pair)]*(1-u)+t.time[t.index(pair+1)]*u;
                float fade=(float)Math.pow(Math.max(0,1-(now-stamp)/1e9/c.length),c.fadeSpeed);
                float alpha=c.alpha*fade*Math.min(1,s/(float)Math.max(1,segments)*5);
                if(c.pulse)alpha*=.8f+.2f*(float)Math.sin(now/3e8-s*.07);
                float fraction=s/(float)segments;
                int color=c.themeColor?ConfigManager.get().theme.accent:c.color;
                if(c.rainbow || "Rainbow".equals(c.mode))color=ColorUtil.rainbow((float)(now/1e10)+fraction*.55f);
                else if(c.gradient || "Gradient".equals(c.mode))color=ColorUtil.mix(color,c.secondaryColor,fraction);
                // Camera-facing strip handles vertical throws as well as horizontal arcs.
                double dx=x-px,dy=y-py,dz=z-pz,cx=camera.x-(px+x)*.5,cy=camera.y-(py+y)*.5,cz=camera.z-(pz+z)*.5;
                double nx=dy*cz-dz*cy,ny=dz*cx-dx*cz,nz=dx*cy-dy*cx;
                double norm=Math.sqrt(nx*nx+ny*ny+nz*nz);
                if(norm>1e-8 && camera.distanceToSqr(px,py,pz)>.36 && camera.distanceToSqr(x,y,z)>.36){
                    nx/=norm;ny/=norm;nz/=norm;
                    // Keep distant paths legible without making nearby throws bulky.
                    float distance=(float)Math.sqrt(cx*cx+cy*cy+cz*cz);
                    float width=Math.max(c.width,Math.min(c.width*3,distance*.0018f))*(.35f+.65f*fraction);
                    width*=Math.min(1,distance/2);
                    if(c.glow || "Glow".equals(c.mode))strip(out,matrix,px,py,pz,x,y,z,nx,ny,nz,width*3,color,alpha*.12f);
                    strip(out,matrix,px,py,pz,x,y,z,nx,ny,nz,width,color,alpha*.65f);
                    strip(out,matrix,px,py,pz,x,y,z,nx,ny,nz,width*.5f,ColorUtil.mix(color,0xFFFFFFFF,.35f),alpha);
                }
                px=x;py=y;pz=z;
            }
        }
        pose.popPose();
    }
    private static void strip(VertexConsumer out,Matrix4fc m,double ax,double ay,double az,double bx,double by,double bz,double nx,double ny,double nz,float width,int color,float alpha){
        double h=width*.5;
        WorldDraw.quad(out,m,ax+nx*h,ay+ny*h,az+nz*h,ax-nx*h,ay-ny*h,az-nz*h,bx-nx*h,by-ny*h,bz-nz*h,bx+nx*h,by+ny*h,bz+nz*h,color,alpha);
    }
}
