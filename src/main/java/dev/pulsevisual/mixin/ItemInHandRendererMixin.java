package dev.pulsevisual.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.pulsevisual.animation.SwingAnimator;
import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @ModifyExpressionValue(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F"))
    private float pulsevisual$visualEquipScale(float original) {
        var swing=ConfigManager.get().swing;
        return swing.enabled && !"Vanilla".equals(swing.preset) ? 1 : original;
    }
    @Unique private static final Quaternionf PULSEVISUAL_ROTATION=new Quaternionf();
    @Inject(method="renderArmWithItem",at=@At("HEAD"))
    private void pulsevisual$begin(AbstractClientPlayer player,float partial,float pitch,InteractionHand hand,
                                   float swing,ItemStack stack,float equip,PoseStack pose,SubmitNodeCollector collector,
                                   int light,CallbackInfo ci) {
        pose.pushPose();
        HumanoidArm physical=hand==InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        float anchorX=physical==HumanoidArm.RIGHT?.56f:-.56f;
        float anchorY=-.52f-equip*.6f;
        // Rotate and scale around the grip, rather than orbiting the camera.
        pose.translate(anchorX,anchorY,-.72f);
        VisualConfig.ViewModel vm=ConfigManager.get().viewModel;
        if(vm.enabled) {
            VisualConfig.Hand h=physical==HumanoidArm.RIGHT ? vm.right : vm.left;
            pose.translate(h.x,h.y,h.z);
            pose.mulPose(PULSEVISUAL_ROTATION.rotationXYZ((float)Math.toRadians(h.rotX),
                    (float)Math.toRadians(h.rotY),(float)Math.toRadians(h.rotZ)));
            pose.scale(h.scale,h.scale,h.scale);
        }
        SwingAnimator.apply(pose,hand,swing);
        pose.translate(-anchorX,-anchorY,.72f);
    }
    @ModifyVariable(method="renderArmWithItem",at=@At("HEAD"),argsOnly=true,ordinal=2)
    private float pulsevisual$remapSwing(float original,AbstractClientPlayer player,float partial,float pitch,
                                        InteractionHand hand) { return SwingAnimator.progress(original,hand); }
    @Inject(method="renderArmWithItem",at=@At("RETURN"))
    private void pulsevisual$end(AbstractClientPlayer player,float partial,float pitch,InteractionHand hand,
                                 float swing,ItemStack stack,float equip,PoseStack pose,SubmitNodeCollector collector,
                                 int light,CallbackInfo ci) { pose.popPose(); }
}
