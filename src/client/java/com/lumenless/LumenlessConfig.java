package com.lumenless;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
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

    public boolean directionalShading = true;

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
                INSTANCE.directionalShading = false;
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
                INSTANCE.directionalShading = true;
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
                INSTANCE.directionalShading = true;
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

    public static boolean applyDirectionalShading(boolean modelShade) {
        return RenderToggleLogic.directionalShade(active(), INSTANCE.directionalShading, modelShade);
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
        INSTANCE.directionalShading = loaded.directionalShading;
    }

    private static void sanitize() {
        if (INSTANCE.preset == null) {
            INSTANCE.preset = Preset.CLARITY;
        }
    }
}
