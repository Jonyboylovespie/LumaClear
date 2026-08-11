package com.lumenless.mixin;

import com.lumenless.LumenlessConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
    private void lumenless$hideVignette(GuiGraphicsExtractor graphics, Entity camera, CallbackInfo ci) {
        if (LumenlessConfig.hideVignette()) {
            ci.cancel();
        }
    }
}
