package com.lumaclear.mixin;

import com.lumaclear.FullbrightLighting;
import net.minecraft.client.renderer.block.BlockModelLighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shared by vanilla and Indigo; the independent shade-brightness/AO cache is untouched. */
@Mixin(BlockModelLighter.Cache.class)
public abstract class BlockLightCacheMixin {
    @Inject(method = "getLightCoords", at = @At("HEAD"), cancellable = true)
    private void lumaclear$skipUnusedLightSamples(CallbackInfoReturnable<Integer> cir) {
        if (FullbrightLighting.active()) {
            cir.setReturnValue(FullbrightLighting.packedLight());
        }
    }
}
