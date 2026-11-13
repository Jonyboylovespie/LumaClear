package com.lumaclear.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lumaclear.DirectionalLighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Indigo multiplies its independently sampled AO by this directional factor. */
@Mixin(targets = "net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator")
public abstract class IndigoAoCalculatorMixin {
    @ModifyExpressionValue(method = "computeFace(Lnet/fabricmc/fabric/impl/client/indigo/renderer/aocalc/AoFaceData;Lnet/minecraft/core/Direction;ZLnet/minecraft/core/Direction;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/CardinalLighting;byFace(Lnet/minecraft/core/Direction;)F"))
    private float lumaclear$removeDirectionalBrightness(float brightness) {
        return DirectionalLighting.apply(brightness);
    }
}
