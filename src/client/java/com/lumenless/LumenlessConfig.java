package com.lumenless;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import com.mojang.blaze3d.vertex.QuadInstance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Small, deliberately dependency-free configuration model. The rendering mixins only read this class, so changing a
 * setting never allocates or touches the hot render path beyond a few primitive checks.
 */
public final class LumenlessConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getConfigDir().resolve("lumenless.json");
    private static final LumenlessConfig INSTANCE = new LumenlessConfig();

    private static volatile boolean renderStateDirty = true;

    public enum Preset {
        VANILLA("Vanilla"),
        FULLBRIGHT("Fullbright"),
        CLARITY("Lumenless Clarity (Recommended)"),
        MAXIMUM("Lumenless Maximum"),
        CUSTOM("Custom");

        private final String displayName;

        Preset(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return this.displayName;
        }
    }

    public boolean enabled = true;
    public Preset preset = Preset.CLARITY;

    public boolean fullbright = true;
    public boolean noDistanceFog = true;
    public boolean noDarknessFog = true;
    public boolean noUnderwaterFog = true;
    public boolean noLavaFog = true;
    public boolean noPowderSnowFog = true;
    public boolean noNetherFog = true;
    public boolean noWeatherFog = true;

    public boolean disableAmbientOcclusion = false;
    public boolean directionalShading = true;
    public float directionalShadingStrength = 1.0F;

    public boolean hideRain = false;
    public boolean hideSnow = false;
    public boolean hideVignette = false;

    private LumenlessConfig() {
    }

    public static LumenlessConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.isRegularFile(FILE)) {
            try {
                LumenlessConfig loaded = GSON.fromJson(Files.readString(FILE), LumenlessConfig.class);
                if (loaded != null) {
                    copyFrom(loaded);
                }
            } catch (IOException | JsonParseException | IllegalStateException exception) {
                System.err.println("[Lumenless] Could not read config: " + exception.getMessage());
            }
        }

        sanitize();
        markRenderStateDirty();
    }

    public static void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (IOException exception) {
            System.err.println("[Lumenless] Could not save config: " + exception.getMessage());
        }
    }

    public static void applyPreset(Preset next) {
        INSTANCE.preset = next;
        switch (next) {
            case VANILLA -> {
                INSTANCE.enabled = false;
                INSTANCE.fullbright = false;
                INSTANCE.noDistanceFog = false;
                INSTANCE.noDarknessFog = false;
                INSTANCE.noUnderwaterFog = false;
                INSTANCE.noLavaFog = false;
                INSTANCE.noPowderSnowFog = false;
                INSTANCE.noNetherFog = false;
                INSTANCE.noWeatherFog = false;
                INSTANCE.disableAmbientOcclusion = false;
                INSTANCE.directionalShading = false;
                INSTANCE.directionalShadingStrength = 1.0F;
                INSTANCE.hideRain = false;
                INSTANCE.hideSnow = false;
                INSTANCE.hideVignette = false;
            }
            case FULLBRIGHT -> {
                INSTANCE.enabled = true;
                INSTANCE.fullbright = true;
                INSTANCE.noDistanceFog = false;
                INSTANCE.noDarknessFog = false;
                INSTANCE.noUnderwaterFog = false;
                INSTANCE.noLavaFog = false;
                INSTANCE.noPowderSnowFog = false;
                INSTANCE.noNetherFog = false;
                INSTANCE.noWeatherFog = false;
                INSTANCE.disableAmbientOcclusion = false;
                INSTANCE.directionalShading = true;
                INSTANCE.directionalShadingStrength = 1.0F;
                INSTANCE.hideRain = false;
                INSTANCE.hideSnow = false;
                INSTANCE.hideVignette = false;
            }
            case CLARITY -> {
                INSTANCE.enabled = true;
                INSTANCE.fullbright = true;
                INSTANCE.noDistanceFog = true;
                INSTANCE.noDarknessFog = true;
                INSTANCE.noUnderwaterFog = true;
                INSTANCE.noLavaFog = true;
                INSTANCE.noPowderSnowFog = true;
                INSTANCE.noNetherFog = true;
                INSTANCE.noWeatherFog = true;
                INSTANCE.disableAmbientOcclusion = false;
                INSTANCE.directionalShading = true;
                INSTANCE.directionalShadingStrength = 1.0F;
                INSTANCE.hideRain = false;
                INSTANCE.hideSnow = false;
                INSTANCE.hideVignette = false;
            }
            case MAXIMUM -> {
                INSTANCE.enabled = true;
                INSTANCE.fullbright = true;
                INSTANCE.noDistanceFog = true;
                INSTANCE.noDarknessFog = true;
                INSTANCE.noUnderwaterFog = true;
                INSTANCE.noLavaFog = true;
                INSTANCE.noPowderSnowFog = true;
                INSTANCE.noNetherFog = true;
                INSTANCE.noWeatherFog = true;
                INSTANCE.disableAmbientOcclusion = true;
                INSTANCE.directionalShading = false;
                INSTANCE.directionalShadingStrength = 1.0F;
                INSTANCE.hideRain = false;
                INSTANCE.hideSnow = false;
                INSTANCE.hideVignette = false;
            }
            case CUSTOM -> INSTANCE.enabled = true;
        }

        save();
        markRenderStateDirty();
    }

    public static void markRenderStateDirty() {
        renderStateDirty = true;
    }

    public static boolean consumeRenderStateDirty() {
        boolean dirty = renderStateDirty;
        renderStateDirty = false;
        return dirty;
    }

    public static boolean active() {
        return INSTANCE.enabled && INSTANCE.preset != Preset.VANILLA;
    }

    public static boolean fullbrightActive() {
        return active() && INSTANCE.fullbright;
    }

    /**
     * True when vanilla/Sodium can skip their light/AO calculation and use a fixed result. Clarity deliberately
     * keeps the native vertex shading: the reference shader removes lightmap darkness but preserves ambient
     * occlusion and face shading, which carry most of the terrain's visible depth.
     */
    public static boolean simplifiedBlockLighting() {
        return fullbrightActive() && INSTANCE.disableAmbientOcclusion;
    }

    public static boolean removeSodiumFogShader() {
        return active()
                && INSTANCE.noDistanceFog
                && INSTANCE.noDarknessFog
                && INSTANCE.noUnderwaterFog
                && INSTANCE.noLavaFog
                && INSTANCE.noPowderSnowFog
                && INSTANCE.noNetherFog
                && INSTANCE.noWeatherFog;
    }

    public static float faceShade(Direction direction, boolean shade) {
        if (!shade || !INSTANCE.directionalShading || direction == null) {
            return 1.0F;
        }

        float base = switch (direction) {
            case UP -> 1.00F;
            case NORTH, SOUTH -> 0.93F;
            case EAST, WEST -> 0.86F;
            case DOWN -> 0.80F;
        };
        float strength = Math.max(0.0F, Math.min(1.0F, INSTANCE.directionalShadingStrength));
        return 1.0F - (1.0F - base) * strength;
    }

    public static boolean applyDirectionalShading(boolean modelShade) {
        return RenderToggleLogic.directionalShade(active(), INSTANCE.directionalShading, modelShade);
    }

    public static void applySimpleQuadLighting(QuadInstance output, Direction direction, boolean shade) {
        output.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
        output.setColor(ARGB.gray(faceShade(direction, shade)));
    }

    public static void applyFog(Camera camera, FogData fog) {
        if (distanceFogDisabled()) {
            fog.renderDistanceStart = Float.MAX_VALUE;
            fog.renderDistanceEnd = Float.MAX_VALUE;
            fog.skyEnd = Float.MAX_VALUE;
            fog.cloudEnd = Float.MAX_VALUE;
        }

        if (environmentalFogDisabledFor(camera)) {
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
        }
    }

    public static boolean distanceFogDisabled() {
        return RenderToggleLogic.settingActive(active(), INSTANCE.noDistanceFog);
    }

    public static boolean weatherFogDisabled() {
        return RenderToggleLogic.settingActive(active(), INSTANCE.noWeatherFog);
    }

    /** True when the camera's current environment-specific fog must be removed. */
    public static boolean environmentalFogDisabledFor(Camera camera) {
        if (!active()) {
            return false;
        }

        boolean environmentFogDisabled = switch (camera.getFluidInCamera()) {
            case WATER -> INSTANCE.noUnderwaterFog;
            case LAVA -> INSTANCE.noLavaFog;
            case POWDER_SNOW -> INSTANCE.noPowderSnowFog;
            case NONE, ATMOSPHERIC -> INSTANCE.noNetherFog && isNether(camera);
        };

        return environmentFogDisabled || (INSTANCE.noDarknessFog && isDarknessEffect(camera.entity()));
    }

    public static boolean hideRain() {
        return active() && INSTANCE.hideRain;
    }

    public static boolean hideSnow() {
        return active() && INSTANCE.hideSnow;
    }

    public static boolean hideVignette() {
        return active() && INSTANCE.hideVignette;
    }

    public static String activePresetName() {
        return INSTANCE.preset.displayName();
    }

    private static boolean isDarknessEffect(Entity entity) {
        return entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.DARKNESS) || living.hasEffect(MobEffects.BLINDNESS));
    }

    private static boolean isNether(Camera camera) {
        return camera.entity() != null && camera.entity().level().dimension() == Level.NETHER;
    }

    private static void copyFrom(LumenlessConfig loaded) {
        INSTANCE.enabled = loaded.enabled;
        INSTANCE.preset = loaded.preset == null ? Preset.CLARITY : loaded.preset;
        INSTANCE.fullbright = loaded.fullbright;
        INSTANCE.noDistanceFog = loaded.noDistanceFog;
        INSTANCE.noDarknessFog = loaded.noDarknessFog;
        INSTANCE.noUnderwaterFog = loaded.noUnderwaterFog;
        INSTANCE.noLavaFog = loaded.noLavaFog;
        INSTANCE.noPowderSnowFog = loaded.noPowderSnowFog;
        INSTANCE.noNetherFog = loaded.noNetherFog;
        INSTANCE.noWeatherFog = loaded.noWeatherFog;
        INSTANCE.disableAmbientOcclusion = loaded.disableAmbientOcclusion;
        INSTANCE.directionalShading = loaded.directionalShading;
        INSTANCE.directionalShadingStrength = loaded.directionalShadingStrength;
        INSTANCE.hideRain = loaded.hideRain;
        INSTANCE.hideSnow = loaded.hideSnow;
        INSTANCE.hideVignette = loaded.hideVignette;
    }

    private static void sanitize() {
        if (INSTANCE.preset == null) {
            INSTANCE.preset = Preset.CLARITY;
        }
        if (!Float.isFinite(INSTANCE.directionalShadingStrength)) {
            INSTANCE.directionalShadingStrength = 1.0F;
        }
        INSTANCE.directionalShadingStrength = Math.max(0.0F, Math.min(1.0F, INSTANCE.directionalShadingStrength));
    }
}
