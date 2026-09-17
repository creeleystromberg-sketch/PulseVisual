package dev.pulsevisual.mixin;

import dev.pulsevisual.animation.SwingAnimator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method="swing(Lnet/minecraft/world/InteractionHand;)V",at=@At("RETURN"))
    private void pulsevisual$startSwing(InteractionHand hand, CallbackInfo ci) {
        LocalPlayer player=(LocalPlayer)(Object)this;
        // -1 is set by vanilla only when it accepts a new swing.
        if(player.swinging && player.swingTime==-1) SwingAnimator.onSwing(hand,player.tickCount);
    }
}
