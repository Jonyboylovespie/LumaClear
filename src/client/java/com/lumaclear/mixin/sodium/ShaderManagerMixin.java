package com.lumaclear.mixin.sodium;

import com.lumaclear.ShaderSourceTransform;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Edits only Sodium's terrain vertex source. Since 26.3 the source provider is
 * ShaderManager's private Configs record, rather than ShaderManager itself.
 */
@Mixin(targets = "net.minecraft.client.renderer.ShaderManager$Configs")
public abstract class ShaderManagerMixin {
    @Unique
    private static final String SODIUM_TERRAIN_SHADER = "blocks/block_layer_opaque";

    @Inject(method = "getShader", at = @At("RETURN"), cancellable = true, require = 1)
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
