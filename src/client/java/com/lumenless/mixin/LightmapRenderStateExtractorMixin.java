package com.lumenless.mixin;

import com.lumenless.LumenlessConfig;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the expensive darkness/gamma/light blend with a stable white lightmap when fullbright is active. */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @Inject(method = "extract", at = @At("HEAD"))
    private void lumenless$markDirty(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (LumenlessConfig.consumeRenderStateDirty()) {
            renderState.needsUpdate = true;
        }
    }

    @Inject(method = "extract", at = @At("RETURN"))
    private void lumenless$applyFullbright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (!LumenlessConfig.fullbrightActive()) {
            return;
        }

        Vector3fc white = LightmapRenderStateExtractor.WHITE;
        renderState.blockFactor = 1.0F;
        renderState.blockLightTint = white;
        renderState.skyFactor = 1.0F;
        renderState.skyLightColor = white;
        renderState.ambientColor = white;
        renderState.brightness = 0.0F;
        renderState.darknessEffectScale = 0.0F;
        renderState.nightVisionEffectIntensity = 0.0F;
        renderState.nightVisionColor = white;
        renderState.bossOverlayWorldDarkening = 0.0F;
    }
}
