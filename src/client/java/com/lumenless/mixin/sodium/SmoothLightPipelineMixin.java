package com.lumenless.mixin.sodium;

import com.lumenless.LumenlessConfig;
import net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SmoothLightPipeline.class)
@Pseudo
public abstract class SmoothLightPipelineMixin {
    @ModifyVariable(method = "calculate", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean lumenless$applyDirectionalShading(boolean shade) {
        return LumenlessConfig.applyDirectionalShading(shade);
    }
}
