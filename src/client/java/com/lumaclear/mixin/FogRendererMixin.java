package com.lumaclear.mixin;

import com.lumaclear.LumaClearFogPolicy;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adjusts the shared vanilla fog state before both vanilla and compatible renderers consume it. */
@Mixin(value = FogRenderer.class, priority = 1200)
public abstract class FogRendererMixin {
    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;isApplicable(Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean lumaclear$selectFogEnvironment(FogEnvironment environment, FogType type, Entity entity) {
        return LumaClearFogPolicy.applicable(environment, type, entity);
    }

    @Redirect(method = "computeFogColor", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;modifiesDarkness()Z"))
    private boolean lumaclear$removeDarknessFogColor(FogEnvironment environment) {
        return LumaClearFogPolicy.modifiesDarkness(environment);
    }

    // Update the completed vanilla state before RETURN callbacks (including Sodium's fog snapshot).
    @Inject(method = "setupFog", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void lumaclear$applyFog(
            Camera camera,
            int renderDistanceInChunks,
            DeltaTracker deltaTracker,
            float darkenWorldAmount,
            ClientLevel level,
            CallbackInfoReturnable<FogData> cir,
            @Local FogData fog
    ) {
        LumaClearFogPolicy.applyDistanceFog(fog);
    }
}
