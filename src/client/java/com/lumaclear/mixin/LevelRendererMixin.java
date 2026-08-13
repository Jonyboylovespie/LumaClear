package com.lumaclear.mixin;

import com.lumaclear.LumaClearConfig;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Keeps the lower clear region continuous with the sky when terrain fog no longer hides their boundary. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private Vector4f lumaclear$matchHorizonToSky(Vector4f clearColor) {
        FogType fogType = this.levelRenderState.cameraRenderState.fogType;
        boolean unobstructedAir = fogType == FogType.NONE || fogType == FogType.ATMOSPHERIC;
        boolean overworldSky = this.levelRenderState.skyRenderState.skybox == DimensionType.Skybox.OVERWORLD;

        if (LumaClearConfig.active()
                && LumaClearConfig.get().noDistanceFog
                && unobstructedAir
                && overworldSky) {
            return ARGB.vector4fFromARGB32(this.levelRenderState.skyRenderState.skyColor);
        }

        return clearColor;
    }
}
