package com.lumaclear.mixin;

import com.lumaclear.DirectionalLighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skips only Indigo's directional color multiplication, including enhanced normal-based shading. */
@Mixin(targets = "net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.FlatLighter")
public abstract class IndigoFlatLighterMixin {
    @Inject(method = "applyDirectionalBrightness", at = @At("HEAD"), cancellable = true)
    private void lumaclear$removeDirectionalBrightness(CallbackInfo ci) {
        if (DirectionalLighting.disabled()) {
            ci.cancel();
        }
    }
}
