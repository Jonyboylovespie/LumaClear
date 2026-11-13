package com.lumaclear.mixin.sodium;

import com.lumaclear.LumaClearConfig;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.oit.OitStage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mixin(ShaderChunkRenderer.class)
@Pseudo
public abstract class ShaderChunkRendererMixin {
    // Keep Sodium's normal-lighting caches intact. Each pass needs at most one
    // extra fullbright variant, including one set containing all three OIT stages.
    // These are immutable pipeline descriptions, not compiled GPU resources:
    // Minecraft recompiles the same descriptions in its new cache after a reload.
    @Unique
    private static final Map<TerrainRenderPass, RenderPipeline> lumaclear$fullbrightPrograms =
            new IdentityHashMap<>();
    @Unique
    private static final Map<TerrainRenderPass, OitPipelineSet> lumaclear$fullbrightOitPrograms =
            new IdentityHashMap<>();

    @Shadow
    private RenderPipeline createShader(String path, TerrainRenderPass pass) {
        throw new AssertionError("Mixin shadow");
    }

    @Shadow
    private OitPipelineSet createOITShader(String path, TerrainRenderPass pass, OitStage stage) {
        throw new AssertionError("Mixin shadow");
    }

    @Inject(method = "compileProgram", at = @At("HEAD"), cancellable = true, require = 1)
    private void lumaclear$selectFullbrightProgram(
            TerrainRenderPass pass,
            OitStage stage,
            CallbackInfoReturnable<RenderPipeline> cir
    ) {
        if (!LumaClearConfig.fullbrightActive()) {
            return;
        }

        if (stage == null) {
            RenderPipeline program = lumaclear$fullbrightPrograms.get(pass);
            if (program == null) {
                program = this.createShader("blocks/block_layer_opaque", pass);
                lumaclear$fullbrightPrograms.put(pass, program);
            }
            cir.setReturnValue(program);
        } else {
            OitPipelineSet programs = lumaclear$fullbrightOitPrograms.get(pass);
            if (programs == null) {
                programs = this.createOITShader("blocks/block_layer_opaque", pass, stage);
                lumaclear$fullbrightOitPrograms.put(pass, programs);
            }
            cir.setReturnValue(programs.getPipeline(stage));
        }
    }

    @Inject(method = "createShaderConstants", at = @At("RETURN"), require = 1)
    private static void lumaclear$addFullbrightDefine(
            TerrainRenderPass pass,
            CallbackInfoReturnable<List<String>> cir
    ) {
        if (LumaClearConfig.fullbrightActive()) {
            cir.getReturnValue().add("LUMACLEAR_FULLBRIGHT");
        }
        // Sodium 0.9.3's 26.3 fragment shader always consumes the fog varyings.
        // Removing USE_FOG leaves the vertex and fragment interfaces mismatched
        // and crashes the first time a world renders, so leave Sodium's define
        // intact. LumaClear's own fog hooks still handle the visibility change.
    }
}
