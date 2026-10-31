package com.lumaclear.mixin;

import net.minecraft.client.renderer.block.BlockModelLighter;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Vanilla 26.3 replaced the boolean MaterialInfo#shade flag with a directional override.
 * Directional shading is handled by the Fabric/Sodium rendering hooks for this release.
 */
@Mixin(BlockModelLighter.class)
public abstract class BlockModelLighterMixin {
}
