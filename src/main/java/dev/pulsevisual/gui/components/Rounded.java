package dev.pulsevisual.gui.components;

import net.minecraft.client.gui.GuiGraphics;

public final class Rounded {
    private Rounded() { }
    public static void fill(GuiGraphics g,int x,int y,int w,int h,int radius,int color) {
        int r=Math.max(0,Math.min(radius,Math.min(w,h)/2));
        if(r==0) { g.fill(x,y,x+w,y+h,color); return; }
        // Non-overlapping scanlines: translucent panels must not blend twice.
        g.fill(x,y+r,x+w,y+h-r,color);
        for(int row=0;row<r;row++) {
            int inset=(int)Math.ceil(r-Math.sqrt(Math.max(0,r*r-(r-row-.5)*(r-row-.5))));
            g.fill(x+inset,y+row,x+w-inset,y+row+1,color);
            g.fill(x+inset,y+h-row-1,x+w-inset,y+h-row,color);
        }
    }
}
