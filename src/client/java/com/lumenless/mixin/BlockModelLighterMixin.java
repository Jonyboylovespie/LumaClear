package com.lumenless.mixin;

import com.lumenless.LumenlessConfig;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies the optional directional-shading toggle to vanilla block geometry. */
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
}
