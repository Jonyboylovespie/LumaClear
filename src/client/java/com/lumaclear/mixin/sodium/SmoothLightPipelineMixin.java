package com.lumaclear.mixin.sodium;

import com.lumaclear.DirectionalLighting;
import net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmoothLightPipeline.class)
@Pseudo
public abstract class SmoothLightPipelineMixin {
    @Inject(method = "getAmbientBrightness", at = @At("HEAD"), cancellable = true, require = 1)
    private void lumaclear$removeDirectionalBrightness(CallbackInfoReturnable<Float> cir) {
        if (DirectionalLighting.disabled()) {
            cir.setReturnValue(1.0F);
        }
    }
}
