package com.lumaclear;

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
    void directionalBrightnessKeepsVanillaAndModelOverridesWhenShadingIsEnabled() {
        for (float brightness : new float[]{0.0F, 0.5F, 0.8F, 0.9F, 1.0F}) {
            assertEquals(brightness, RenderToggleLogic.directionalBrightness(false, false, brightness));
            assertEquals(brightness, RenderToggleLogic.directionalBrightness(false, true, brightness));
            assertEquals(brightness, RenderToggleLogic.directionalBrightness(true, true, brightness));
        }
    }

    @Test
    void disablingDirectionalBrightnessPreservesTheSeparateAmbientOcclusionFactor() {
        float ambientOcclusion = 0.625F;
        float directionalBrightness = RenderToggleLogic.directionalBrightness(true, false, 0.6F);

        assertEquals(1.0F, directionalBrightness);
        assertEquals(ambientOcclusion, ambientOcclusion * directionalBrightness);
    }

    @Test
    void weatherFogIsRemovedOnlyWhenItsIndependentToggleIsActive() {
        assertEquals(0.75F, RenderToggleLogic.weatherFogMultiplier(false, true, 0.75F));
        assertEquals(0.75F, RenderToggleLogic.weatherFogMultiplier(true, false, 0.75F));
        assertEquals(0.0F, RenderToggleLogic.weatherFogMultiplier(true, true, 0.75F));
    }
}
