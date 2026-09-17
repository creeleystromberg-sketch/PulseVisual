package dev.pulsevisual.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4fc;

final class WorldDraw {
    private WorldDraw() { }
    static void vertex(VertexConsumer out,Matrix4fc matrix,double x,double y,double z,int color,float alpha) {
        out.addVertex(matrix,(float)x,(float)y,(float)z)
                .setColor((color>>16)&255,(color>>8)&255,color&255,Math.max(0,Math.min(255,Math.round(alpha*255))));
    }
    static void quad(VertexConsumer out,Matrix4fc m,double ax,double ay,double az,double bx,double by,double bz,
                     double cx,double cy,double cz,double dx,double dy,double dz,int color,float alpha) {
        vertex(out,m,ax,ay,az,color,alpha); vertex(out,m,bx,by,bz,color,alpha);
        vertex(out,m,cx,cy,cz,color,alpha); vertex(out,m,dx,dy,dz,color,alpha);
    }
    static void line(VertexConsumer out,Matrix4fc m,double ax,double ay,double az,double bx,double by,double bz,int color,float alpha) {
        out.addVertex(m,(float)ax,(float)ay,(float)az).setColor((color>>16)&255,(color>>8)&255,color&255,(int)(alpha*255)).setNormal(0,1,0).setLineWidth(1.7f);
        out.addVertex(m,(float)bx,(float)by,(float)bz).setColor((color>>16)&255,(color>>8)&255,color&255,(int)(alpha*255)).setNormal(0,1,0).setLineWidth(1.7f);
    }
}
