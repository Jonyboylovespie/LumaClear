package com.lumenless.mixin;

import com.lumenless.LumenlessConfig;
import com.lumenless.RenderToggleLogic;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Removes only the extra atmospheric fog introduced by rain and snow. */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {
    @Shadow
    private float rainFogMultiplier;

    @Inject(method = "updateRainFogState", at = @At("RETURN"))
    private void lumenless$removeWeatherFog(
            Camera camera,
            ClientLevel level,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        this.rainFogMultiplier = RenderToggleLogic.weatherFogMultiplier(
                LumenlessConfig.active(),
                LumenlessConfig.get().noWeatherFog,
                this.rainFogMultiplier
        );
    }
}
