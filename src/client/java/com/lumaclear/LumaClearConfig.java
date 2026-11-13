package com.lumaclear;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persisted settings and simple activation checks; Minecraft fog decisions live in LumaClearFogPolicy. */
public final class LumaClearConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getConfigDir().resolve("lumaclear.json");
    private static final LumaClearConfig INSTANCE = new LumaClearConfig();

    private static volatile boolean renderStateDirty = true;

    public enum Preset {
        FULLBRIGHT("Fullbright"),
        CLARITY("LumaClear Clarity (Recommended)"),
        CUSTOM("Custom");

        private final String displayName;

        Preset(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return this.displayName;
        }
    }

    public volatile boolean enabled = true;
    public Preset preset = Preset.CLARITY;

    public volatile boolean fullbright = true;
    public boolean noDistanceFog = true;
    public boolean noDarknessFog = true;
    public boolean noUnderwaterFog = true;
    public boolean noLavaFog = true;
    public boolean noPowderSnowFog = true;
    public boolean noNetherFog = true;
    public boolean noWeatherFog = true;

    public boolean directionalShading = true;

    private LumaClearConfig() {
    }

    public static LumaClearConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.isRegularFile(FILE)) {
            try {
                LumaClearConfig loaded = GSON.fromJson(Files.readString(FILE), LumaClearConfig.class);
                if (loaded != null) {
                    copyFrom(loaded);
                }
            } catch (IOException | JsonParseException | IllegalStateException exception) {
                System.err.println("[LumaClear] Could not read config: " + exception.getMessage());
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
            System.err.println("[LumaClear] Could not save config: " + exception.getMessage());
        }
    }

    public static void applyPreset(Preset next) {
        INSTANCE.preset = next;
        switch (next) {
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
        return INSTANCE.enabled;
    }

    public static boolean fullbrightActive() {
        return active() && INSTANCE.fullbright;
    }

    public static boolean distanceFogDisabled() {
        return RenderToggleLogic.settingActive(active(), INSTANCE.noDistanceFog);
    }

    public static boolean weatherFogDisabled() {
        return RenderToggleLogic.settingActive(active(), INSTANCE.noWeatherFog);
    }

    public static String activePresetName() {
        return INSTANCE.preset.displayName();
    }

    private static void copyFrom(LumaClearConfig loaded) {
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
