package com.lumenless;

/** Pure toggle decisions shared by the renderer integrations and their unit tests. */
public final class RenderToggleLogic {
    private RenderToggleLogic() {
    }

    public static boolean settingActive(boolean modActive, boolean settingEnabled) {
        return modActive && settingEnabled;
    }

    public static boolean directionalShade(boolean modActive, boolean directionalShading, boolean modelShade) {
        return modelShade && (!modActive || directionalShading);
    }

    public static float weatherFogMultiplier(
            boolean modActive,
            boolean weatherFogDisabled,
            float vanillaMultiplier
    ) {
        return settingActive(modActive, weatherFogDisabled) ? 0.0F : vanillaMultiplier;
    }
}
