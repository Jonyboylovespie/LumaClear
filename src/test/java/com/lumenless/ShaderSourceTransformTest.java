package com.lumenless;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaderSourceTransformTest {
    @Test
    void wrapsTheSodiumLightmapLookupInAFullbrightVariant() {
        String source = "before\n" + ShaderSourceTransform.LIGHTMAP_EXPRESSION + "\nafter";

        String transformed = ShaderSourceTransform.addFullbrightTerrainVariant(source);

        assertTrue(transformed.contains("#ifdef LUMENLESS_FULLBRIGHT"));
        assertTrue(transformed.contains("v_Color = _vert_color;"));
        assertTrue(transformed.contains(ShaderSourceTransform.LIGHTMAP_EXPRESSION));
        assertSame(transformed, ShaderSourceTransform.addFullbrightTerrainVariant(transformed));
    }

    @Test
    void leavesUnknownShaderVersionsUntouched() {
        String source = "void main() { gl_Position = vec4(0.0); }";

        assertSame(source, ShaderSourceTransform.addFullbrightTerrainVariant(source));
        assertEquals(null, ShaderSourceTransform.addFullbrightTerrainVariant(null));
    }
}
