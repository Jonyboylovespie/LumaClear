package com.lumaclear;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaderSourceTransformTest {
    @Test
    void wrapsTheSodiumLightmapLookupInAFullbrightVariant() {
        String source = "before\n" + ShaderSourceTransform.LIGHTMAP_EXPRESSION + "\nafter";

        String transformed = ShaderSourceTransform.addFullbrightTerrainVariant(source);

        assertTrue(transformed.contains("#ifdef LUMACLEAR_FULLBRIGHT"));
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

    @Test
    void preservesSodiumsAlphaOnlyOitBranchAndShaderVersion() {
        String source = """
                #version 330
                void main() {
                #ifndef OIT_ALPHA_ONLY
                    v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);
                #else
                    v_Color = _vert_color;
                #endif
                }
                """;

        String transformed = ShaderSourceTransform.addFullbrightTerrainVariant(source);

        // All OIT stages share this source. Fullbright must guard only the
        // lightmap branch, leaving alpha-only stages and #version untouched.
        assertEquals("""
                #version 330
                void main() {
                #ifndef OIT_ALPHA_ONLY
                    #ifdef LUMACLEAR_FULLBRIGHT
                    v_Color = _vert_color;
                #else
                    v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);
                #endif
                #else
                    v_Color = _vert_color;
                #endif
                }
                """, transformed);
        assertSame(transformed, ShaderSourceTransform.addFullbrightTerrainVariant(transformed));
    }
}
