package com.lumaclear.smoke;

import com.lumaclear.LumaClearConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import org.joml.Vector3f;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/** Exercises transformed rendering methods with CPU-only inputs, before a game window exists. */
public final class DirectionalSmokeChecks {
    private static final CardinalLighting LIGHTING = new CardinalLighting(0.2F, 0.95F, 0.6F, 0.7F, 0.4F, 0.5F);

    private DirectionalSmokeChecks() {
    }

    public static void run() throws ReflectiveOperationException {
        LumaClearConfig config = LumaClearConfig.get();
        boolean enabled = config.enabled;
        boolean directionalShading = config.directionalShading;
        try {
            checkVanilla(config);
            if (FabricLoader.getInstance().isModLoaded("sodium")) {
                SodiumChecks.run(config);
            }
        } finally {
            config.enabled = enabled;
            config.directionalShading = directionalShading;
        }
        System.out.println("LumaClear directional behavior checks passed");
    }

    private static void checkVanilla(LumaClearConfig config) throws ReflectiveOperationException {
        Method brightness = BlockModelLighter.class.getDeclaredMethod(
                "getDirectionalBrightness", CardinalLighting.class, BakedQuad.class, Direction.class);
        brightness.setAccessible(true);

        for (boolean enabled : new boolean[]{false, true}) {
            for (boolean shading : new boolean[]{false, true}) {
                config.enabled = enabled;
                config.directionalShading = shading;
                for (Direction face : Direction.values()) {
                    for (Direction override : new Direction[]{null, Direction.DOWN, Direction.UP}) {
                        BakedQuad.MaterialInfo material = new BakedQuad.MaterialInfo(
                                null, null, null, null, null, -1, override, 0);
                        Vector3f position = new Vector3f();
                        BakedQuad quad = new BakedQuad(position, position, position, position, 0L, 0L, 0L, 0L, face, material);
                        float expected = enabled && !shading ? 1.0F : LIGHTING.byFace(override == null ? face : override);
                        close(expected, (Float)brightness.invoke(null, LIGHTING, quad, face),
                                "vanilla directional brightness/override");
                    }
                }
            }
        }
    }

    private static BlockAndTintGetter lightingOnlyLevel() {
        return (BlockAndTintGetter)Proxy.newProxyInstance(
                DirectionalSmokeChecks.class.getClassLoader(), new Class<?>[]{BlockAndTintGetter.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("cardinalLighting")) {
                        return LIGHTING;
                    }
                    throw new AssertionError("Directional test unexpectedly queried the world: " + method);
                });
    }

    private static void close(float expected, float actual, String label) {
        if (!Float.isFinite(actual) || Math.abs(expected - actual) > 0.0001F) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    /** Kept in a nested class so no Sodium class is resolved when the optional mod is absent. */
    private static final class SodiumChecks {
        private static final float AO = 0.625F;
        private static final int LIGHT = net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess.packBL(7)
                | net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess.packSL(11)
                | net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess.packAO(AO);

        private static void run(LumaClearConfig config) {
            net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess cache =
                    new net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess() {
                        {
                            this.level = lightingOnlyLevel();
                        }

                        @Override
                        public int get(int x, int y, int z) {
                            return LIGHT;
                        }
                    };
            net.caffeinemc.mods.sodium.client.model.light.LightPipeline[] pipelines = {
                    new net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline(cache),
                    new net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline(cache),
                    new net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline(cache)
            };
            for (net.caffeinemc.mods.sodium.client.model.light.LightPipeline pipeline : pipelines) {
                boolean smooth = pipeline instanceof net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
                for (boolean enhanced : new boolean[]{false, true}) {
                    for (boolean aligned : new boolean[]{false, true}) {
                        for (Direction override : new Direction[]{null, Direction.DOWN}) {
                            checkPipeline(config, pipeline, smooth, enhanced, aligned, override);
                        }
                    }
                }
            }
        }

        private static void checkPipeline(
                LumaClearConfig config,
                net.caffeinemc.mods.sodium.client.model.light.LightPipeline pipeline,
                boolean smooth,
                boolean enhanced,
                boolean aligned,
                Direction override
        ) {
            net.caffeinemc.mods.sodium.client.model.quad.ModelQuad quad = new net.caffeinemc.mods.sodium.client.model.quad.ModelQuad();
            int normal = net.caffeinemc.mods.sodium.api.util.NormI8.pack(1.0F, 0.0F, 0.0F);
            quad.setFaceNormal(normal);
            quad.setLightFace(Direction.WEST);
            quad.setFlags(aligned ? 4 : 0);
            for (int i = 0; i < 4; i++) {
                quad.setX(i, 0.25F);
                quad.setY(i, 0.25F);
                quad.setZ(i, 0.25F);
                quad.setNormal(i, normal);
            }

            float directionalFactor = LIGHTING.byFace(override != null ? override : !aligned && enhanced ? Direction.EAST : Direction.WEST);
            float ambientOcclusion = smooth ? AO : 1.0F;
            int[] referenceLightmap = null;
            for (boolean enabled : new boolean[]{false, true}) {
                for (boolean shading : new boolean[]{false, true}) {
                    config.enabled = enabled;
                    config.directionalShading = shading;
                    net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData result =
                            new net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData();
                    pipeline.calculate(quad, BlockPos.ZERO, result, aligned ? Direction.WEST : null, Direction.WEST, override, enhanced);
                    float expected = ambientOcclusion * (enabled && !shading ? 1.0F : directionalFactor);
                    String label = pipeline.getClass().getSimpleName() + ", enhanced=" + enhanced
                            + ", aligned=" + aligned + ", override=" + override + ", enabled=" + enabled + ", shading=" + shading;
                    for (float brightness : result.br) {
                        close(expected, brightness, label);
                    }
                    if (referenceLightmap == null) {
                        referenceLightmap = result.lm.clone();
                    } else if (!Arrays.equals(referenceLightmap, result.lm)) {
                        throw new AssertionError("Directional toggle changed sampled light: " + label);
                    }
                }
            }
        }
    }
}
