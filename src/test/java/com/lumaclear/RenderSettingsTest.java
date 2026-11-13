package com.lumaclear;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderSettingsTest {
    @Test
    void fogAndPresetLabelChangesDoNotInvalidateRenderingResources() {
        RenderSettings before = new RenderSettings(true, true);
        RenderSettings after = new RenderSettings(true, true);
        assertFalse(before.needsGeometryRebuild(after));
        assertFalse(before.needsLightmapUpdate(after));
    }

    @Test
    void fullbrightTransitionsRestoreLightSamplesAndRebuildGeometry() {
        for (boolean fullbright : new boolean[]{false, true}) {
            RenderSettings before = new RenderSettings(fullbright, true);
            RenderSettings after = new RenderSettings(!fullbright, true);
            assertTrue(before.needsLightmapUpdate(after));
            assertTrue(before.needsGeometryRebuild(after));
        }
    }

    @Test
    void effectiveDirectionalBrightnessChangesRebuildGeometry() {
        for (boolean fullbright : new boolean[]{false, true}) {
            for (boolean directional : new boolean[]{false, true}) {
                RenderSettings before = new RenderSettings(fullbright, directional);
                RenderSettings after = new RenderSettings(fullbright, !directional);
                assertTrue(before.needsGeometryRebuild(after));
                assertFalse(before.needsLightmapUpdate(after));
            }
        }
    }
}
