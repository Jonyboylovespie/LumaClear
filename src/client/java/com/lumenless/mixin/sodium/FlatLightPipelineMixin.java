package com.lumenless.mixin.sodium;

import com.lumenless.LumenlessConfig;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FlatLightPipeline.class)
@Pseudo
public abstract class FlatLightPipelineMixin {
    @ModifyVariable(method = "calculate", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean lumenless$applyDirectionalShading(boolean shade) {
        return LumenlessConfig.applyDirectionalShading(shade);
    }
}
