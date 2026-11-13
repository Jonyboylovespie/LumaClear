package com.lumaclear;

/** Pure toggle decisions shared by the renderer integrations and their unit tests. */
public final class RenderToggleLogic {
    private RenderToggleLogic() {
    }

    public static boolean settingActive(boolean modActive, boolean settingEnabled) {
        return modActive && settingEnabled;
    }

    public static float directionalBrightness(boolean modActive, boolean directionalShading, float brightness) {
        return modActive && !directionalShading ? 1.0F : brightness;
    }

    public static float weatherFogMultiplier(
            boolean modActive,
            boolean weatherFogDisabled,
            float vanillaMultiplier
    ) {
        return settingActive(modActive, weatherFogDisabled) ? 0.0F : vanillaMultiplier;
    }
}
