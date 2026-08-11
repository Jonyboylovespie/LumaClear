package com.lumenless.mixin.sodium;

import com.lumenless.LumenlessConfig;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(FlatFluidLightPipeline.class)
@Pseudo
public abstract class FlatFluidLightPipelineMixin {
    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true)
    private void lumenless$replaceLighting(
            ModelQuadView quad,
            BlockPos pos,
            QuadLightData out,
            Direction cullFace,
            Direction lightFace,
            boolean shade,
            boolean enhanced,
            CallbackInfo ci
    ) {
        if (LumenlessConfig.simplifiedBlockLighting()) {
            Arrays.fill(out.br, LumenlessConfig.faceShade(lightFace, shade));
            Arrays.fill(out.lm, LightCoordsUtil.FULL_BRIGHT);
            ci.cancel();
        }
    }
}
