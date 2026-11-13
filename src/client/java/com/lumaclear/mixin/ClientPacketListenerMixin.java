package com.lumaclear.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.lumaclear.FullbrightLighting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Incoming light packets still update world light storage; only their terrain rebuild is disposable. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @WrapWithCondition(method = "readSectionList", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/multiplayer/ClientLevel;setSectionDirtyWithNeighbors(III)V"))
    private boolean lumaclear$skipInvisiblePacketLightRebuild(ClientLevel level, int x, int y, int z) {
        return !FullbrightLighting.active();
    }
}
