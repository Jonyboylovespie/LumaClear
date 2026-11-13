package com.lumaclear.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lumaclear.DirectionalLighting;
import com.lumaclear.FullbrightLighting;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.world.level.CardinalLighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** FluidRenderer reads cardinal components directly, including the product used for side faces. */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Inject(method = "getLightCoords", at = @At("HEAD"), cancellable = true)
    private void lumaclear$skipUnusedFluidLight(CallbackInfoReturnable<Integer> cir) {
        if (FullbrightLighting.active()) {
            cir.setReturnValue(FullbrightLighting.packedLight());
        }
    }

    @ModifyExpressionValue(method = "tesselate", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/block/BlockAndTintGetter;cardinalLighting()Lnet/minecraft/world/level/CardinalLighting;"))
    private CardinalLighting lumaclear$removeDirectionalBrightness(CardinalLighting lighting) {
        return DirectionalLighting.apply(lighting);
    }
}
