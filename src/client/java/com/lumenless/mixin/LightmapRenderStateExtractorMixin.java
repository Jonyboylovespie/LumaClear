package com.lumenless.mixin;

import com.lumenless.LumenlessConfig;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the expensive darkness/gamma/light blend with a stable white lightmap when fullbright is active. */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @Shadow
    private boolean needsUpdate;

    @Unique
    private boolean lumenless$wasFullbright;

    /** Keeps vanilla's animated lightmap work dormant while the uploaded map is a stable fullbright map. */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void lumenless$skipFullbrightTick(CallbackInfo ci) {
        boolean fullbright = LumenlessConfig.fullbrightActive();
        if (fullbright) {
            if (!this.lumenless$wasFullbright) {
                this.needsUpdate = true;
            }
            this.lumenless$wasFullbright = true;
            ci.cancel();
        } else if (this.lumenless$wasFullbright) {
            this.lumenless$wasFullbright = false;
            this.needsUpdate = true;
        }
    }

    /** Bypasses environment probes, status-effect blending, gamma work, and boss-overlay calculations. */
    @Inject(method = "extract", at = @At("HEAD"), cancellable = true)
    private void lumenless$extractFixedFullbright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (LumenlessConfig.consumeRenderStateDirty()) {
            this.needsUpdate = true;
        }

        if (!LumenlessConfig.fullbrightActive()) {
            return;
        }

        renderState.needsUpdate = this.needsUpdate;
        if (!this.needsUpdate) {
            ci.cancel();
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
        this.needsUpdate = false;
        ci.cancel();
    }
}
