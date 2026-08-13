package com.lumaclear.mixin.sodium;

import com.lumaclear.LumaClearConfig;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShaderChunkRenderer.class)
@Pseudo
public abstract class ShaderChunkRendererMixin {
    @Inject(method = "createShaderConstants", at = @At("RETURN"))
    private static void lumaclear$removeFogDefine(
            TerrainRenderPass pass,
            CallbackInfoReturnable<List<String>> cir
    ) {
        if (LumaClearConfig.fullbrightActive()) {
            cir.getReturnValue().add("LUMACLEAR_FULLBRIGHT");
        }
        if (LumaClearConfig.removeSodiumFogShader()) {
            cir.getReturnValue().remove("USE_FOG");
        }
    }
}
