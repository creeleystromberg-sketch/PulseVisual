package dev.pulsevisual.mixin;
import dev.pulsevisual.render.PearlTracer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method="handleMovePlayer",at=@At("RETURN"))
    private void pulsevisual$teleport(CallbackInfo ci){PearlTracer.clearAfterTeleport();}
}
