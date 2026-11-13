package com.lumaclear;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.client.renderer.fog.environment.PowderedSnowFogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;

/** Adapts settings to Minecraft's existing fog providers without replacing their priority or fog colors. */
public final class LumaClearFogPolicy {
    private LumaClearFogPolicy() {
    }

    public static int disabled() {
        LumaClearConfig config = LumaClearConfig.get();
        if (!config.enabled) {
            return 0;
        }
        return (config.noDistanceFog ? FogPolicy.DISTANCE : 0)
                | (config.noDarknessFog ? FogPolicy.DARKNESS : 0)
                | (config.noUnderwaterFog ? FogPolicy.WATER : 0)
                | (config.noLavaFog ? FogPolicy.LAVA : 0)
                | (config.noPowderSnowFog ? FogPolicy.POWDER_SNOW : 0)
                | (config.noNetherFog ? FogPolicy.NETHER : 0)
                | (config.noWeatherFog ? FogPolicy.WEATHER : 0);
    }

    public static boolean applicable(FogEnvironment environment, FogType type, Entity entity) {
        int disabled = disabled();
        boolean vanillaApplicable = environment.isApplicable(type, entity);
        if (disabled == 0) {
            return vanillaApplicable;
        }
        FogPolicy.Environment medium = switch (type) {
            case LAVA -> FogPolicy.Environment.LAVA;
            case POWDER_SNOW -> FogPolicy.Environment.POWDER_SNOW;
            case WATER -> FogPolicy.Environment.WATER;
            case NONE, ATMOSPHERIC -> FogPolicy.Environment.ATMOSPHERIC;
        };
        return FogPolicy.applicable(environment(environment), vanillaApplicable,
                FogPolicy.removed(medium, disabled), disabled);
    }

    private static FogPolicy.Environment environment(FogEnvironment environment) {
        if (environment instanceof LavaFogEnvironment) return FogPolicy.Environment.LAVA;
        if (environment instanceof PowderedSnowFogEnvironment) return FogPolicy.Environment.POWDER_SNOW;
        if (environment instanceof BlindnessFogEnvironment) return FogPolicy.Environment.BLINDNESS;
        if (environment instanceof DarknessFogEnvironment) return FogPolicy.Environment.DARKNESS;
        if (environment instanceof WaterFogEnvironment) return FogPolicy.Environment.WATER;
        if (environment instanceof AtmosphericFogEnvironment) return FogPolicy.Environment.ATMOSPHERIC;
        return FogPolicy.Environment.OTHER;
    }

    public static boolean modifiesDarkness(FogEnvironment environment) {
        return environment.modifiesDarkness() && !FogPolicy.removed(environment(environment), disabled());
    }

    public static void applyDistanceFog(FogData fog) {
        if (LumaClearConfig.distanceFogDisabled()) {
            fog.renderDistanceStart = Float.MAX_VALUE;
            fog.renderDistanceEnd = Float.MAX_VALUE;
        }
    }

    /** Called only when atmospheric fog actually wins provider selection, preserving retained media/effect caps. */
    public static void applyAtmosphericFog(
            FogData fog, Camera camera, ClientLevel level, float renderDistance,
            DeltaTracker deltaTracker, float rainFogMultiplier
    ) {
        int disabled = disabled();
        boolean baseRemoved = FogPolicy.atmosphericBaseRemoved(level.dimension() == Level.NETHER, disabled);
        if (!baseRemoved && (disabled & FogPolicy.DISTANCE) == 0) {
            return;
        }
        if (baseRemoved) {
            fog.environmentalStart = FogPolicy.rainFogStart(renderDistance, rainFogMultiplier);
            fog.environmentalEnd = FogPolicy.rainFogEnd(renderDistance, rainFogMultiplier);
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        fog.skyEnd = FogPolicy.atmosphericEnd(
                camera.attributeProbe().getValue(EnvironmentAttributes.SKY_FOG_END_DISTANCE, partialTick),
                renderDistance, baseRemoved, disabled);
        fog.cloudEnd = FogPolicy.atmosphericEnd(
                camera.attributeProbe().getValue(EnvironmentAttributes.CLOUD_FOG_END_DISTANCE, partialTick),
                Minecraft.getInstance().options.cloudRange().get() * 16.0F, baseRemoved, disabled);

        // Boss fog is independent of ordinary dimension/distance fog.
        if (Minecraft.getInstance().gui.hud.getBossOverlay().shouldCreateWorldFog()) {
            fog.environmentalStart = Math.min(fog.environmentalStart, 10.0F);
            fog.environmentalEnd = Math.min(fog.environmentalEnd, 96.0F);
            fog.skyEnd = fog.environmentalEnd;
            fog.cloudEnd = fog.environmentalEnd;
        }
    }
}
