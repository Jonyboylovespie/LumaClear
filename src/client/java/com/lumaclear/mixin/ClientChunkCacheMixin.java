package com.lumaclear.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.lumaclear.FullbrightLighting;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Suppress only the render invalidation from a light notification, never a geometry change. */
@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {
    @WrapWithCondition(method = "onLightUpdate", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/extract/LevelExtractor;setSectionDirty(III)V"))
    private boolean lumaclear$skipInvisibleLightRebuild(LevelExtractor extractor, int x, int y, int z) {
        return !FullbrightLighting.active();
    }
}
