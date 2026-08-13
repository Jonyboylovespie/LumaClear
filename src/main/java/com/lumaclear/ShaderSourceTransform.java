package com.lumaclear;

/** Small, testable shader edit used by the optional Sodium integration. */
public final class ShaderSourceTransform {
    static final String LIGHTMAP_EXPRESSION =
            "v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);";
    static final String GUARDED_FULLBRIGHT_EXPRESSION = """
            #ifdef LUMACLEAR_FULLBRIGHT
                v_Color = _vert_color;
            #else
                v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);
            #endif""";

    private ShaderSourceTransform() {
    }

    public static String addFullbrightTerrainVariant(String source) {
        if (source == null
                || source.contains("#ifdef LUMACLEAR_FULLBRIGHT")
                || !source.contains(LIGHTMAP_EXPRESSION)) {
            return source;
        }
        return source.replace(LIGHTMAP_EXPRESSION, GUARDED_FULLBRIGHT_EXPRESSION);
    }
}
