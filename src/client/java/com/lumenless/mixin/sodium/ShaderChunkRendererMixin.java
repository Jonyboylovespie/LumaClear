package com.lumenless.mixin.sodium;

import com.lumenless.LumenlessConfig;
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
    private static void lumenless$removeFogDefine(
            TerrainRenderPass pass,
            CallbackInfoReturnable<List<String>> cir
    ) {
        if (LumenlessConfig.fullbrightActive()) {
            cir.getReturnValue().add("LUMENLESS_FULLBRIGHT");
        }
        if (LumenlessConfig.removeSodiumFogShader()) {
            cir.getReturnValue().remove("USE_FOG");
        }
    }
}
