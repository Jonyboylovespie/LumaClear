package com.lumenless.mixin.sodium;

import com.lumenless.LumenlessConfig;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(FlatLightPipeline.class)
@Pseudo
public abstract class FlatLightPipelineMixin {
    @ModifyVariable(method = "calculate", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean lumenless$applyDirectionalShading(boolean shade) {
        return LumenlessConfig.applyDirectionalShading(shade);
    }

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
