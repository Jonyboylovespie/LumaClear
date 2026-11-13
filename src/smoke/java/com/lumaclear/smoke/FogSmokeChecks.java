package com.lumaclear.smoke;

import com.lumaclear.LumaClearConfig;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.client.renderer.fog.environment.PowderedSnowFogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/** Exercises actual transformed fog handlers without constructing a world, renderer, or GPU device. */
public final class FogSmokeChecks {
    private FogSmokeChecks() {
    }

    public static void run() throws ReflectiveOperationException {
        LumaClearConfig config = LumaClearConfig.get();
        Map<Field, Object> previous = new LinkedHashMap<>();
        for (Field field : LumaClearConfig.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                previous.put(field, field.get(config));
            }
        }
        try {
            Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeType.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object renderer = unsafeType.getMethod("allocateInstance", Class.class)
                    .invoke(unsafeField.get(null), FogRenderer.class);
            Object levelRenderer = unsafeType.getMethod("allocateInstance", Class.class)
                    .invoke(unsafeField.get(null), LevelRenderer.class);
            checkHorizon(config, levelRenderer);
            Method applicable = handler("lumaclear$selectFogEnvironment");
            Method modifiesDarkness = handler("lumaclear$removeDarknessFogColor");
            Method applyFog = handler("lumaclear$applyFog");

            FogEnvironment lava = new LavaFogEnvironment();
            FogEnvironment snow = new PowderedSnowFogEnvironment();
            FogEnvironment water = new WaterFogEnvironment();
            FogEnvironment atmosphere = new AtmosphericFogEnvironment();
            // Only applicability is stubbed: these represent active status effects,
            // retaining the real provider type and darkness behavior under test.
            FogEnvironment blindness = new BlindnessFogEnvironment() {
                @Override
                public boolean isApplicable(FogType type, Entity entity) {
                    return true;
                }
            };
            FogEnvironment darkness = new DarknessFogEnvironment() {
                @Override
                public boolean isApplicable(FogType type, Entity entity) {
                    return true;
                }
            };

            LumaClearConfig.applyPreset(LumaClearConfig.Preset.FULLBRIGHT);
            config.noDarknessFog = true;
            require(select(renderer, applicable, FogType.LAVA, lava, blindness, atmosphere) == lava,
                    "Removing darkness must retain higher-priority lava fog");
            require(select(renderer, applicable, FogType.POWDER_SNOW, snow, darkness, atmosphere) == snow,
                    "Removing darkness must retain higher-priority powder-snow fog");
            require(select(renderer, applicable, FogType.WATER, blindness, water, atmosphere) == water,
                    "Removing blindness must restore underwater fog");
            require(!(Boolean)modifiesDarkness.invoke(renderer, blindness),
                    "Removed blindness must not darken the remaining fog color");

            config.noDarknessFog = false;
            config.noNetherFog = true;
            require(select(renderer, applicable, FogType.ATMOSPHERIC, blindness, atmosphere) == blindness,
                    "Removing Nether fog must retain blindness fog");
            config.noUnderwaterFog = true;
            require(select(renderer, applicable, FogType.WATER, blindness, water, atmosphere) == blindness,
                    "Removing underwater fog must retain blindness fog");
            require(select(renderer, applicable, FogType.WATER, water, atmosphere) == atmosphere,
                    "Removing underwater fog must fall back to atmospheric fog");
            config.noLavaFog = true;
            require(select(renderer, applicable, FogType.LAVA, lava, darkness, atmosphere) == darkness,
                    "Removing lava fog must preserve darkness fog");

            config.enabled = false;
            require(select(renderer, applicable, FogType.WATER, water, atmosphere) == water,
                    "Disabled mod must restore vanilla medium selection");
            require((Boolean)modifiesDarkness.invoke(renderer, darkness),
                    "Disabled mod must restore vanilla darkness fog color");

            config.enabled = true;
            config.noDistanceFog = true;
            FogData fog = new FogData();
            fog.environmentalStart = 1.0F;
            fog.environmentalEnd = 5.0F;
            fog.skyEnd = 4.0F;
            fog.cloudEnd = 4.0F;
            fog.renderDistanceStart = 128.0F;
            fog.renderDistanceEnd = 160.0F;
            applyFog.invoke(renderer, null, 10, null, 0.0F, null,
                    new CallbackInfoReturnable<FogData>("setupFog", false), fog);
            require(fog.renderDistanceStart == Float.MAX_VALUE && fog.renderDistanceEnd == Float.MAX_VALUE,
                    "Distance fog ranges were not removed");
            require(fog.environmentalStart == 1.0F && fog.environmentalEnd == 5.0F
                            && fog.skyEnd == 4.0F && fog.cloudEnd == 4.0F,
                    "Distance removal must preserve selected medium/effect fog ranges");
        } finally {
            for (Map.Entry<Field, Object> entry : previous.entrySet()) {
                entry.getKey().set(config, entry.getValue());
            }
        }
        System.out.println("LUMACLEAR_FOG_BEHAVIOR_SMOKE_PASSED");
    }

    private static void checkHorizon(LumaClearConfig config, Object renderer) throws ReflectiveOperationException {
        LevelRenderState state = new LevelRenderState();
        state.cameraRenderState.fogType = FogType.ATMOSPHERIC;
        state.cameraRenderState.entityRenderState.doesMobEffectBlockSky = true;
        state.skyRenderState.skybox = DimensionType.Skybox.OVERWORLD;
        state.skyRenderState.skyColor = new Vector3f(0.3F, 0.5F, 0.9F);
        Field stateField = LevelRenderer.class.getDeclaredField("levelRenderState");
        stateField.setAccessible(true);
        stateField.set(renderer, state);
        Method horizon = null;
        for (Method method : LevelRenderer.class.getDeclaredMethods()) {
            if (method.getName().contains("lumaclear$matchHorizonToSky")) horizon = method;
        }
        require(horizon != null, "Horizon guard mixin is missing");
        horizon.setAccessible(true);
        Vector4f original = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
        config.enabled = true;
        config.noDistanceFog = true;
        config.noDarknessFog = false;
        require(horizon.invoke(renderer, original) == original,
                "Retained darkness must keep its clear color even with distance fog removed");
        config.noDarknessFog = true;
        require(horizon.invoke(renderer, original) != original,
                "Removed darkness must allow the ordinary horizon correction");
        config.enabled = false;
        require(horizon.invoke(renderer, original) == original,
                "Disabled mod must preserve the original clear color");
    }

    private static Method handler(String name) {
        for (Method method : FogRenderer.class.getDeclaredMethods()) {
            if (method.getName().contains(name)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new AssertionError("Missing transformed fog handler " + name);
    }

    private static FogEnvironment select(
            Object renderer, Method applicable, FogType medium, FogEnvironment... providers
    ) throws ReflectiveOperationException {
        for (FogEnvironment provider : providers) {
            if ((Boolean)applicable.invoke(renderer, provider, medium, null)) {
                return provider;
            }
        }
        throw new AssertionError("No fog provider selected for " + medium);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
