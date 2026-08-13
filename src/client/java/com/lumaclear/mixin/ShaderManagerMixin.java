package com.lumaclear.mixin;

import com.lumaclear.ShaderSourceTransform;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Removes Sodium's terrain lightmap lookup without carrying a forked copy of Sodium's shader. */
@Mixin(ShaderManager.class)
public abstract class ShaderManagerMixin {
    private static final String SODIUM_TERRAIN_SHADER = "blocks/block_layer_opaque";
    @Inject(method = "getShader", at = @At("RETURN"), cancellable = true)
    private void lumaclear$removeTerrainLightmapLookup(
            Identifier id,
            ShaderType type,
            CallbackInfoReturnable<String> cir
    ) {
        if (type != ShaderType.VERTEX
                || !"sodium".equals(id.getNamespace())
                || !SODIUM_TERRAIN_SHADER.equals(id.getPath())) {
            return;
        }

        String source = cir.getReturnValue();
        String transformed = ShaderSourceTransform.addFullbrightTerrainVariant(source);
        if (transformed != source) {
            cir.setReturnValue(transformed);
        }
    }
}
