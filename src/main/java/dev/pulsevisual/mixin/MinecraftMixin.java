package dev.pulsevisual.mixin;

import dev.pulsevisual.animation.SwingAnimator;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    // continueAttack is the repeated block-digging path, not a new mouse attack.
    // Observe its scope only; vanilla digging and packets run unmodified.
    @Inject(method="continueAttack",at=@At("HEAD"))
    private void pulsevisual$beginMining(boolean held,CallbackInfo ci){SwingAnimator.continuousMining=true;}
    @Inject(method="continueAttack",at=@At("RETURN"))
    private void pulsevisual$endMining(boolean held,CallbackInfo ci){SwingAnimator.continuousMining=false;}
}
