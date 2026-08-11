package com.lumenless.mixin.sodium;

import com.lumenless.LumenlessConfig;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Prevents Sodium's fog-aware section culler from discarding terrain that Lumenless makes visible. */
@Mixin(SodiumWorldRenderer.class)
@Pseudo
public abstract class SodiumWorldRendererMixin {
    @ModifyVariable(method = "setupTerrain", at = @At("HEAD"), argsOnly = true)
    private FogParameters lumenless$disableFogCulling(FogParameters original) {
        if (!LumenlessConfig.fogDisabledFor(Minecraft.getInstance().gameRenderer.mainCamera())) {
            return original;
        }

        float disabled = Float.MAX_VALUE;
        return new FogParameters(
                original.red(),
                original.green(),
                original.blue(),
                original.alpha(),
                disabled,
                disabled,
                disabled,
                disabled
        );
    }
}
