package dev.pulsevisual.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.pulsevisual.config.ConfigManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(method="renderFire",at=@At("HEAD"),cancellable=true)
    private static void pulsevisual$fire(PoseStack pose,MultiBufferSource buffers,TextureAtlasSprite sprite,CallbackInfo ci){
        float size=ConfigManager.get().fire.size/100f;
        if(size<=0){ci.cancel();return;}
        pose.pushPose();
        // Anchor at the lower viewport edge; preserve vanilla width and texture.
        pose.translate(0,-.35f*(1-size),0);
        pose.scale(1,size,1);
    }
    @Inject(method="renderFire",at=@At("RETURN"))
    private static void pulsevisual$restore(PoseStack pose,MultiBufferSource buffers,TextureAtlasSprite sprite,CallbackInfo ci){
        pose.popPose();
    }
}
