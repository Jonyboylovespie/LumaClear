package com.lumaclear.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lumaclear.DirectionalLighting;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlatFluidLightPipeline.class)
@Pseudo
public abstract class FlatFluidLightPipelineMixin {
    @ModifyExpressionValue(method = "calculate", require = 1, at = @At(value = "INVOKE", target =
            "Lnet/caffeinemc/mods/sodium/client/services/PlatformBlockAccess;getNormalVectorShade(Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Z)F"))
    private float lumaclear$removeNormalVectorShade(float brightness) {
        return DirectionalLighting.apply(brightness);
    }
}
