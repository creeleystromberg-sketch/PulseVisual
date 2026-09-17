package dev.pulsevisual.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pulsevisual.config.ConfigManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class GuiMixin {
    /** Keep vanilla sprite, resource packs, visibility rules and attack indicator. */
    @Redirect(method="renderCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",ordinal=0))
    private void pulsevisual$vanillaCrosshair(GuiGraphics g,RenderPipeline pipeline,Identifier sprite,int x,int y,int width,int height){
        var c=ConfigManager.get();
        if(!c.customizeCrosshair){g.blitSprite(pipeline,sprite,x,y,width,height);return;}
        // Odd, integer dimensions keep the centre pixel and all arms aligned.
        int size=Math.max(5,Math.round(width*c.crosshairSize))|1;
        int color=(Math.round(255*c.crosshairOpacity)<<24)|0xFFFFFF;
        g.blitSprite(pipeline,sprite,(g.guiWidth()-size)/2,(g.guiHeight()-size)/2,size,size,color);
    }
}
