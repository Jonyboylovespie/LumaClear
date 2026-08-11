package com.lumenless.mixin;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.lumenless.LumenlessConfig;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Short-circuits vanilla AO/light sampling for the maximum preset while retaining biome tinting downstream. */
@Mixin(BlockModelLighter.class)
public abstract class BlockModelLighterMixin {
    @Redirect(
            method = {"prepareQuadAmbientOcclusion", "prepareQuadFlat"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;shade()Z"
            )
    )
    private boolean lumenless$applyDirectionalShading(BakedQuad.MaterialInfo materialInfo) {
        return LumenlessConfig.applyDirectionalShading(materialInfo.shade());
    }

    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("HEAD"), cancellable = true)
    private void lumenless$replaceAmbientOcclusion(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos centerPosition,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
        if (LumenlessConfig.simplifiedBlockLighting()) {
            LumenlessConfig.applySimpleQuadLighting(outputInstance, quad.direction(), quad.materialInfo().shade());
            ci.cancel();
        }
    }

    @Inject(method = "prepareQuadFlat", at = @At("HEAD"), cancellable = true)
    private void lumenless$replaceFlatLighting(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            int lightCoords,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
        if (LumenlessConfig.simplifiedBlockLighting()) {
            LumenlessConfig.applySimpleQuadLighting(outputInstance, quad.direction(), quad.materialInfo().shade());
            ci.cancel();
        }
    }
}
