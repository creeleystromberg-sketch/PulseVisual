package dev.pulsevisual.gui;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import dev.pulsevisual.gui.components.*;
import dev.pulsevisual.module.Module;
import dev.pulsevisual.module.ModuleManager;
import dev.pulsevisual.util.ColorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** All geometry and input use the same logical canvas, including popovers. */
public final class SettingsScreen extends Screen {
    private final Screen parent;
    private final Map<Setting<?>,Float> hover=new HashMap<>();
    private int tab,category,channel,pickerX,pickerY,dropX,dropY;
    private float scale=1,open,scroll,targetScroll;
    private long lastFrame=System.nanoTime();
    private boolean preview,closing;
    private NumberSetting dragging,editing;
    private EditBox editor;
    private ColorSetting picker,colorDrag;
    private ModeSetting dropdown;
    public SettingsScreen(Screen parent){super(Component.literal("PulseVisual"));this.parent=parent;}
    private int vw(){return (int)(width/scale);}
    private int vh(){return (int)(height/scale);}
    private int w(){return preview?480:650;}
    private int h(){return Math.min(440,vh()-16);}
    private int x(){return preview?8:(vw()-w())/2;}
    private int y(){return(vh()-h())/2;}
    private int cx(){return x()+142;}
    private int cw(){return w()-158;}
    private int top(){return y()+95;}
    private int bottom(){return y()+h()-36;}
    private int rowHeight(){return 47;}
    private Module module(){return ModuleManager.in(ModuleManager.CATEGORIES[category]).getFirst();}
    private float maxScroll(){return Math.max(0,module().visibleSettings().size()*rowHeight()-(bottom()-top()));}
    private int radius(){return "Minimal".equals(ConfigManager.get().theme.style)?2:Math.round(ConfigManager.get().theme.rounding);}
    private Component label(String s){
        var t=ConfigManager.get().theme;
        Style style=Style.EMPTY;
        if("Uniform".equals(t.font))style=style.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("uniform")));
        if("Bold".equals(t.font))style=style.withBold(true);
        return Component.literal(s).setStyle(style);
    }
    private void text(GuiGraphics g,String s,int x,int y,int color){g.drawString(font,label(s),x,y,color,ConfigManager.get().theme.textShadow);}
    private void fitted(GuiGraphics g,String s,int x,int y,int max,int color){
        if(font.width(label(s))>max){while(!s.isEmpty()&&font.width(label(s+"…"))>max)s=s.substring(0,s.length()-1);s+="…";}
        text(g,s,x,y,color);
    }
    private void button(GuiGraphics g,int bx,int by,int bw,String s,boolean active){
        var t=ConfigManager.get().theme;
        Rounded.fill(g,bx,by,bw,21,Math.max(2,radius()-3),active?ColorUtil.mix(t.primary,t.enabled,.7f):ColorUtil.mix(t.primary,t.background,.6f));
        fitted(g,s,bx+6,by+7,bw-10,active?t.accent:t.text);
    }
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float delta){
        if(minecraft.level==null)super.renderBackground(g,mx,my,delta);
        else if(!preview && "Glass".equals(ConfigManager.get().theme.style) && ConfigManager.get().theme.blur>.04f)g.blurBeforeThisStratum();
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
        var t=ConfigManager.get().theme;
        long now=System.nanoTime();float dt=Math.min(.05f,(now-lastFrame)/1e9f);lastFrame=now;
        if(dragging==null && colorDrag==null && editor==null){
            float fit=Math.min(width/(float)(w()+16),height/330f);
            scale=Math.max(.1f,Math.min(fit,Math.min(1,fit)*.82f*t.guiScale));
        }
        float mx=mouseX/scale,my=mouseY/scale;
        open=t.animations?open+((closing?0:1)-open)*Math.min(1,dt*16):(closing?0:1);
        if(closing && open<.025f){ConfigManager.save();minecraft.setScreen(parent);return;}
        targetScroll=Math.min(targetScroll,maxScroll());
        scroll=t.animations?scroll+(targetScroll-scroll)*Math.min(1,dt*18):targetScroll;
        if(!preview)g.fill(0,0,width,height,ColorUtil.alpha(t.background,.30f*open));
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        int px=x(),py=y(),pw=w(),ph=h(),r=radius();
        Rounded.fill(g,px-3,py-2,pw+6,ph+6,r+3,ColorUtil.alpha(0xFF000000,.3f*open));
        Rounded.fill(g,px,py,pw,ph,r,ColorUtil.alpha(t.primary,("Solid".equals(t.style)?1:t.opacity)*open));
        text(g,"PULSE",px+16,py+15,t.text);text(g,"VISUAL",px+62,py+15,t.accent);
        text(g,"PERSONALIZE YOUR GAME",px+16,py+30,ColorUtil.mix(t.text,t.primary,.45f));
        button(g,px+pw-91,py+13,75,preview?"Full menu":"Preview",preview);
        for(int i=0;i<3;i++)button(g,px+142+i*92,py+48,86,ModuleManager.TABS[i],i==tab);
        g.fill(px+14,py+79,px+pw-14,py+80,ColorUtil.alpha(t.accent,.18f));
        int[] groups=ModuleManager.GROUPS[tab];
        for(int i=0;i<groups.length;i++){
            int yy=top()+i*31;
            button(g,px+12,yy,117,ModuleManager.CATEGORIES[groups[i]],category==groups[i]);
        }
        g.enableScissor(cx(),top(),cx()+cw(),bottom());
        int yy=top()-Math.round(scroll);
        for(Setting<?> setting:module().visibleSettings()){
            if(yy+rowHeight()>top() && yy<bottom())row(g,setting,yy,(int)mx,(int)my,dt);
            yy+=rowHeight();
        }
        g.disableScissor();
        if(maxScroll()>0){
            int track=bottom()-top(),thumb=Math.max(18,(int)(track*track/(track+maxScroll())));
            int sy=top()+(int)((track-thumb)*scroll/maxScroll());
            Rounded.fill(g,cx()+cw()+3,sy,2,thumb,1,ColorUtil.alpha(t.accent,.7f));
        }
        button(g,cx(),y()+h()-28,125,category==1?"Reset View Model":"Reset section",false);
        text(g,"LIVE",cx()+cw()-31,y()+h()-20,t.accent);
        text(g,"R-SHIFT / ESC",x()+15,y()+h()-20,ColorUtil.mix(t.text,t.primary,.4f));
        if(picker!=null)renderPicker(g);
        if(dropdown!=null)renderDropdown(g);
        if(editor!=null)renderEditor(g,(int)mx,(int)my,delta);
        g.pose().popMatrix();
    }
    private int sx(){return cx()+28;}
    private int sw(){return cw()-166;}
    private void row(GuiGraphics g,Setting<?> s,int yy,int mx,int my,float dt){
        var t=ConfigManager.get().theme;
        float goal=mx>=cx()&&mx<cx()+cw()&&my>=yy&&my<yy+43?1:0;
        float hv=hover.compute(s,(k,v)->v==null?goal:v+(goal-v)*Math.min(1,dt*14));
        Rounded.fill(g,cx(),yy,cw(),43,Math.max(2,radius()-2),ColorUtil.alpha(t.background,.32f+.16f*hv));
        fitted(g,s.name(),cx()+10,yy+8,cw()-20,t.text);
        if(s instanceof NumberSetting n){
            int sy=yy+31;
            text(g,"−",cx()+10,yy+27,t.accent);
            Rounded.fill(g,sx(),sy-2,sw(),4,2,ColorUtil.mix(t.primary,t.text,.2f));
            Rounded.fill(g,sx(),sy-2,Math.max(2,Math.round(sw()*n.fraction())),4,2,t.control);
            Rounded.fill(g,sx()+Math.round(sw()*n.fraction())-3,sy-5,6,10,3,t.text);
            text(g,"+",sx()+sw()+9,yy+27,t.accent);
            Rounded.fill(g,cx()+cw()-107,yy+22,61,18,4,ColorUtil.mix(t.primary,t.background,.3f));
            String value=n.integer?Integer.toString(Math.round(n.get())):String.format(Locale.ROOT,"%.3f",n.get());
            fitted(g,value,cx()+cw()-103,yy+27,54,t.text);
            text(g,"Reset",cx()+cw()-37,yy+27,t.accent);
        } else if(s instanceof BooleanSetting b){
            int bx=cx()+cw()-42;
            Rounded.fill(g,bx,yy+23,30,14,7,b.get()?t.enabled:ColorUtil.mix(t.primary,t.text,.2f));
            Rounded.fill(g,bx+(b.get()?17:3),yy+25,10,10,5,b.get()?t.control:t.text);
            text(g,b.get()?"Enabled":"Disabled",cx()+10,yy+27,ColorUtil.mix(t.text,t.primary,.35f));
        } else if(s instanceof ModeSetting m){fitted(g,m.get()+"  ▾",cx()+10,yy+27,cw()-20,t.accent);}
        else if(s instanceof ColorSetting c){
            Rounded.fill(g,cx()+cw()-53,yy+23,40,14,4,c.get());
            text(g,String.format("#%06X",c.get()&0xFFFFFF),cx()+10,yy+27,t.accent);
        } else if(s instanceof ActionSetting)text(g,"Apply →",cx()+10,yy+27,t.accent);
    }
    private void numberAt(double mx){
        if(dragging!=null)dragging.preview(dragging.min+Math.max(0,Math.min(1,(float)(mx-sx())/sw()))*(dragging.max-dragging.min));
    }
    private void renderDropdown(GuiGraphics g){
        var t=ConfigManager.get().theme;
        Rounded.fill(g,dropX-2,dropY-2,172,dropdown.options.length*22+8,6,0xFF080D16);
        for(int i=0;i<dropdown.options.length;i++)button(g,dropX,dropY+i*22,168,dropdown.options[i],dropdown.options[i].equals(dropdown.get()));
    }
    private void renderPicker(GuiGraphics g){
        var t=ConfigManager.get().theme;
        Rounded.fill(g,pickerX-3,pickerY-3,210,113,7,t.primary);
        text(g,picker.name(),pickerX+8,pickerY+7,t.text);
        float[] hsv=Color.RGBtoHSB((picker.get()>>16)&255,(picker.get()>>8)&255,picker.get()&255,null);
        for(int c=0;c<3;c++){
            int yy=pickerY+28+c*25;
            for(int i=0;i<60;i++){
                float f=i/59f;
                int rgb=switch(c){case 0->Color.HSBtoRGB(f,hsv[1],hsv[2]);case 1->Color.HSBtoRGB(hsv[0],f,hsv[2]);default->Color.HSBtoRGB(hsv[0],hsv[1],f);};
                g.fill(pickerX+22+i*3,yy,pickerX+25+i*3,yy+10,rgb);
            }
            text(g,new String[]{"H","S","V"}[c],pickerX+7,yy+1,t.text);
            int mark=pickerX+22+Math.round(hsv[c]*177);g.fill(mark-1,yy-2,mark+2,yy+12,t.text);
        }
    }
    private void colorAt(double mx){
        float[] hsv=Color.RGBtoHSB((colorDrag.get()>>16)&255,(colorDrag.get()>>8)&255,colorDrag.get()&255,null);
        hsv[channel]=Math.max(0,Math.min(1,(float)(mx-pickerX-22)/177f));
        colorDrag.preview(0xFF000000|(Color.HSBtoRGB(hsv[0],hsv[1],hsv[2])&0xFFFFFF));
    }
    private void startEdit(NumberSetting n){
        editing=n;editor=new EditBox(font,vw()/2-95,vh()/2-5,190,20,Component.literal(n.name()));
        editor.setMaxLength(18);editor.setValue(Float.toString(n.get()));editor.setFocused(true);editor.setHighlightPos(0);
        editor.setResponder(s->{try{float v=Float.parseFloat(s);if(Float.isFinite(v))n.preview(v);}catch(NumberFormatException ignored){}});
    }
    private void renderEditor(GuiGraphics g,int mx,int my,float delta){
        var t=ConfigManager.get().theme;
        g.fill(0,0,vw(),vh(),0x99000000);Rounded.fill(g,vw()/2-110,vh()/2-37,220,82,8,t.primary);
        text(g,editing.name(),vw()/2-95,vh()/2-25,t.text);editor.render(g,mx,my,delta);
        text(g,"Enter / click outside to apply",vw()/2-95,vh()/2+25,t.accent);
    }
    private void finishEdit(){if(editor!=null){editor=null;editing=null;ConfigManager.save();}}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        if(e.button()!=0)return false;
        double mx=e.x()/scale,my=e.y()/scale;
        if(editor!=null){
            if(mx<vw()/2-95 || mx>vw()/2+95 || my<vh()/2-5 || my>vh()/2+15)finishEdit();
            return true;
        }
        if(picker!=null){
            if(mx>=pickerX+22&&mx<=pickerX+202&&my>=pickerY+26&&my<pickerY+91){channel=Math.min(2,((int)my-pickerY-26)/25);colorDrag=picker;colorAt(mx);return true;}
            picker=null;return true;
        }
        if(dropdown!=null){
            int i=(int)((my-dropY)/22);
            if(mx>=dropX&&mx<dropX+168&&my>=dropY&&i>=0&&i<dropdown.options.length)dropdown.set(dropdown.options[i]);
            dropdown=null;return true;
        }
        if(mx>=x()+w()-91&&mx<x()+w()-16&&my>=y()+13&&my<y()+34){preview=!preview;return true;}
        for(int i=0;i<3;i++)if(mx>=x()+142+i*92&&mx<x()+228+i*92&&my>=y()+48&&my<y()+69){
            tab=i;category=ModuleManager.GROUPS[i][0];scroll=targetScroll=0;return true;
        }
        int[] groups=ModuleManager.GROUPS[tab];
        for(int i=0;i<groups.length;i++)if(mx>=x()+12&&mx<x()+129&&my>=top()+i*31&&my<top()+i*31+21){category=groups[i];scroll=targetScroll=0;return true;}
        if(mx>=cx()&&mx<cx()+125&&my>=y()+h()-28&&my<y()+h()-7){ConfigManager.resetModule(module().id());return true;}
        if(mx>=cx()&&mx<cx()+cw()&&my>=top()&&my<bottom()){
            int yy=top()-Math.round(scroll);
            for(Setting<?> s:module().visibleSettings()){
                if(my>=yy&&my<yy+43){
                    if(s instanceof NumberSetting n){
                        if(my<yy+21)return true;
                        float step=n.integer?1:(n.max-n.min>5?1:.01f);
                        if(mx>=cx()+cw()-42)n.set(n.resetValue);
                        else if(mx>=cx()+cw()-110)startEdit(n);
                        else if(mx<sx()-4)n.set(n.get()-step);
                        else if(mx>sx()+sw()+3)n.set(n.get()+step);
                        else {dragging=n;numberAt(mx);}
                    } else if(s instanceof BooleanSetting b)b.toggle();
                    else if(s instanceof ActionSetting a)a.run();
                    else if(s instanceof ModeSetting m){dropdown=m;dropX=Math.min(vw()-176,cx()+cw()-172);dropY=Math.max(4,Math.min(vh()-m.options.length*22-8,yy+23));}
                    else if(s instanceof ColorSetting c){picker=c;pickerX=Math.min(vw()-211,cx()+cw()-208);pickerY=Math.max(5,Math.min(vh()-118,yy+24));}
                    return true;
                }
                yy+=rowHeight();
            }
        }
        return false;
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
        if(dragging!=null){numberAt(e.x()/scale);return true;}
        if(colorDrag!=null){colorAt(e.x()/scale);return true;}return false;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){
        if(dragging!=null||colorDrag!=null){dragging=null;colorDrag=null;ConfigManager.save();return true;}return false;
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        if(editor!=null||dropdown!=null||picker!=null)return true;
        if(mx/scale>=cx()&&my/scale>=top()&&my/scale<bottom()){targetScroll=Math.max(0,Math.min(maxScroll(),targetScroll-(float)vertical*47));return true;}return false;
    }
    @Override public boolean keyPressed(KeyEvent e){
        if(editor!=null){if(e.key()==GLFW.GLFW_KEY_ENTER||e.key()==GLFW.GLFW_KEY_ESCAPE){finishEdit();return true;}return editor.keyPressed(e);}
        if(e.key()==GLFW.GLFW_KEY_ESCAPE||e.key()==GLFW.GLFW_KEY_RIGHT_SHIFT){if(picker!=null||dropdown!=null){picker=null;dropdown=null;}else onClose();return true;}
        return super.keyPressed(e);
    }
    @Override public boolean charTyped(CharacterEvent e){return editor!=null&&editor.charTyped(e);}
    @Override public void onClose(){finishEdit();closing=true;picker=null;dropdown=null;dragging=null;colorDrag=null;}
    @Override public void removed(){ConfigManager.save();}
    @Override public boolean isPauseScreen(){return false;}
}
