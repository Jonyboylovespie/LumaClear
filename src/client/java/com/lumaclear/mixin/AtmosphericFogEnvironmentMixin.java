package com.lumaclear.mixin;

import com.lumaclear.LumaClearConfig;
import com.lumaclear.LumaClearFogPolicy;
import com.lumaclear.RenderToggleLogic;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.FogData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies independent atmospheric distance, Nether, and weather settings to the selected fog provider. */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {
    @Shadow
    private float rainFogMultiplier;

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void lumaclear$applyAtmosphericFog(
            FogData fog, Camera camera, ClientLevel level, float renderDistance,
            DeltaTracker deltaTracker, CallbackInfo ci
    ) {
        LumaClearFogPolicy.applyAtmosphericFog(fog, camera, level, renderDistance, deltaTracker, this.rainFogMultiplier);
    }

    @Inject(method = "updateRainFogState", at = @At("RETURN"))
    private void lumaclear$removeWeatherFog(
            Camera camera,
            ClientLevel level,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        this.rainFogMultiplier = RenderToggleLogic.weatherFogMultiplier(
                LumaClearConfig.active(),
                LumaClearConfig.get().noWeatherFog,
                this.rainFogMultiplier
        );
    }
}
