package com.lumenless.mixin;

import com.lumenless.LumenlessConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void lumenless$adjustWeather(
            ClientLevel level,
            float partialTicks,
            Vec3 cameraPos,
            WeatherRenderState renderState,
            CallbackInfo ci
    ) {
        if (LumenlessConfig.hideRain()) {
            renderState.rainColumns.clear();
        }
        if (LumenlessConfig.hideSnow()) {
            renderState.snowColumns.clear();
        }
    }
}
