package com.lumenless.mixin.sodium;

import com.lumenless.LumenlessConfig;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FlatFluidLightPipeline.class)
@Pseudo
public abstract class FlatFluidLightPipelineMixin {
    @ModifyVariable(method = "calculate", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean lumenless$applyDirectionalShading(boolean shade) {
        return LumenlessConfig.applyDirectionalShading(shade);
    }
}
