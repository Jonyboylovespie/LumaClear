package com.lumaclear;

import net.minecraft.world.level.CardinalLighting;

/** Shared directional-light policy; ambient occlusion and sampled light are deliberately separate. */
public final class DirectionalLighting {
    private static final CardinalLighting UNSHADED = new CardinalLighting(1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);

    private DirectionalLighting() {
    }

    public static boolean disabled() {
        return LumaClearConfig.active() && !LumaClearConfig.get().directionalShading;
    }

    public static float apply(float brightness) {
        return RenderToggleLogic.directionalBrightness(
                LumaClearConfig.active(), LumaClearConfig.get().directionalShading, brightness);
    }

    public static CardinalLighting apply(CardinalLighting lighting) {
        return disabled() ? UNSHADED : lighting;
    }
}
