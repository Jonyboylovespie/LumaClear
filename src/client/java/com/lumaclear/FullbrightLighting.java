package com.lumaclear;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.LightCoordsUtil;

/** Only renderer light samples are disposable; world lighting and geometry-derived AO remain live. */
public final class FullbrightLighting {
    private static final boolean SHADER_RENDERER_PRESENT = FabricLoader.getInstance().isModLoaded("iris");

    private FullbrightLighting() {
    }

    public static boolean active() {
        // Shaderpacks can use light coordinates for more than brightness.
        return !SHADER_RENDERER_PRESENT && LumaClearConfig.fullbrightActive();
    }

    public static int packedLight() {
        return LightCoordsUtil.FULL_BRIGHT;
    }
}
