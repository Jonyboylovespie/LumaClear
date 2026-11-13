package com.lumaclear;

/** Effective settings whose changes require work beyond the next frame's fog uniforms. */
public record RenderSettings(boolean fullbright, boolean directionalShading) {
    public boolean needsLightmapUpdate(RenderSettings next) {
        return fullbright != next.fullbright;
    }

    public boolean needsGeometryRebuild(RenderSettings next) {
        // Fullbright meshes can contain constant light and omit light-only invalidations.
        // Both transitions rebuild, including cancellation/replacement of in-flight chunk work.
        return fullbright != next.fullbright || directionalShading != next.directionalShading;
    }
}
