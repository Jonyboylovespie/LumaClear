package com.lumaclear.mixin;

import com.lumaclear.DirectionalLighting;
import net.minecraft.client.renderer.block.BlockModelLighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Changes only the directional multiplier shared by vanilla's flat and ambient-occlusion paths. */
@Mixin(BlockModelLighter.class)
public abstract class BlockModelLighterMixin {
    @Inject(method = "getDirectionalBrightness", at = @At("HEAD"), cancellable = true)
    private static void lumaclear$removeDirectionalBrightness(CallbackInfoReturnable<Float> cir) {
        if (DirectionalLighting.disabled()) {
            cir.setReturnValue(1.0F);
        }
    }
}
