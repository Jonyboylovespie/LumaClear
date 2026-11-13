package com.lumaclear.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.lumaclear.FullbrightLighting;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Keep Sodium's occlusion/shape/emission flags and AO sampling; bypass only sky/block light reads. */
@Mixin(LightDataAccess.class)
@Pseudo
public abstract class LightDataAccessMixin {
    @WrapOperation(method = "compute", require = 1, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/util/LightCoordsUtil;getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I"))
    private int lumaclear$skipUnusedPackedLight(LightCoordsUtil.BrightnessGetter getter,
            BlockAndLightGetter level, BlockState state, BlockPos pos, Operation<Integer> original) {
        return FullbrightLighting.active() ? FullbrightLighting.packedLight() : original.call(getter, level, state, pos);
    }

    @WrapOperation(method = "compute", require = 2, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/block/BlockAndTintGetter;getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I"))
    private int lumaclear$skipUnusedEmissiveLight(BlockAndTintGetter level, LightLayer layer,
            BlockPos pos, Operation<Integer> original) {
        return FullbrightLighting.active() ? 15 : original.call(level, layer, pos);
    }
}
