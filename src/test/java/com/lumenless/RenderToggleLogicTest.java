package com.lumenless;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderToggleLogicTest {
    @Test
    void settingsOnlyApplyWhileTheModIsActive() {
        assertFalse(RenderToggleLogic.settingActive(false, true));
        assertFalse(RenderToggleLogic.settingActive(true, false));
        assertTrue(RenderToggleLogic.settingActive(true, true));
    }

    @Test
    void directionalShadingPreservesTheModelsShadeFlagOutsideLumenless() {
        assertTrue(RenderToggleLogic.directionalShade(false, false, true));
        assertFalse(RenderToggleLogic.directionalShade(false, true, false));
    }

    @Test
    void directionalShadingCanBeDisabledWithoutChangingAmbientOcclusion() {
        assertTrue(RenderToggleLogic.directionalShade(true, true, true));
        assertFalse(RenderToggleLogic.directionalShade(true, false, true));
        assertFalse(RenderToggleLogic.directionalShade(true, true, false));
    }

    @Test
    void weatherFogIsRemovedOnlyWhenItsIndependentToggleIsActive() {
        assertEquals(0.75F, RenderToggleLogic.weatherFogMultiplier(false, true, 0.75F));
        assertEquals(0.75F, RenderToggleLogic.weatherFogMultiplier(true, false, 0.75F));
        assertEquals(0.0F, RenderToggleLogic.weatherFogMultiplier(true, true, 0.75F));
    }
}
