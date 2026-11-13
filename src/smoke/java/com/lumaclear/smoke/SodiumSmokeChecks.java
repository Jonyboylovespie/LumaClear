package com.lumaclear.smoke;

import com.lumaclear.LumaClearConfig;
import com.lumaclear.ShaderSourceTransform;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/** Exercises transformed Sodium code without constructing a window or GPU device. */
public final class SodiumSmokeChecks {
    private static final String FULLBRIGHT_DEFINE = "LUMACLEAR_FULLBRIGHT";

    private SodiumSmokeChecks() {
    }

    public static void run() throws Exception {
        checkPipelineVariants();
        checkShaderSourceProvider();
        System.out.println("LUMACLEAR_SODIUM_BEHAVIOR_SMOKE_PASSED");
    }

    private static void checkPipelineVariants() throws Exception {
        Class<?> rendererType = Class.forName(
                "net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer");
        Class<?> passType = Class.forName(
                "net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass");
        Class<?> stageType = Class.forName("net.minecraft.client.renderer.oit.OitStage");
        Method compileProgram = rendererType.getDeclaredMethod("compileProgram", passType, stageType);
        compileProgram.setAccessible(true);

        // DefaultChunkRenderer's constructor initializes GPU state. Allocate a
        // test shell instead: compileProgram only reads vertexFormat and builds
        // immutable descriptions; no rendering method is called on this shell.
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeType.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Object renderer = unsafeType.getMethod("allocateInstance", Class.class).invoke(unsafe,
                Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer"));
        Field formatField = rendererType.getDeclaredField("vertexFormat");
        formatField.setAccessible(true);
        formatField.set(renderer, Class.forName(
                "net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex")
                .getField("VERTEX_FORMAT").get(null));

        Object[] passes = (Object[]) Class.forName(
                "net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses")
                .getField("ALL").get(null);
        Object[] stages = stageType.getEnumConstants();
        Map<Variant, RenderPipeline> variants = new HashMap<>();
        Set<RenderPipeline> identities = Collections.newSetFromMap(new IdentityHashMap<>());

        LumaClearConfig config = LumaClearConfig.get();
        boolean previousEnabled = config.enabled;
        boolean previousFullbright = config.fullbright;
        boolean previousDistanceFog = config.noDistanceFog;
        try {
            for (int iteration = 0; iteration < 100; iteration++) {
                // Exercise both fullbright toggles and master-disabled states,
                // while varying a fog setting that must not create new shaders.
                int state = iteration % 4;
                config.enabled = state < 2;
                config.fullbright = state == 1 || state == 2;
                config.noDistanceFog = ((iteration / 4) & 1) == 0;
                boolean fullbright = config.enabled && config.fullbright;
                for (Object pass : passes) {
                    checkVariant(renderer, compileProgram, pass, null, fullbright, variants, identities);
                    for (Object stage : stages) {
                        checkVariant(renderer, compileProgram, pass, stage, fullbright, variants, identities);
                    }
                }
            }
        } finally {
            config.enabled = previousEnabled;
            config.fullbright = previousFullbright;
            config.noDistanceFog = previousDistanceFog;
        }

        int expected = 2 * passes.length * (stages.length + 1);
        require(variants.size() == expected, "Not all lighting/pass/OIT combinations were exercised");
        require(identities.size() == expected,
                "Lighting variants must remain distinct and bounded across repeated toggles");
        System.out.println("Sodium reused " + expected + " pipeline descriptions across 100 settings cycles");
    }

    private static void checkVariant(
            Object renderer,
            Method compileProgram,
            Object pass,
            Object stage,
            boolean fullbright,
            Map<Variant, RenderPipeline> variants,
            Set<RenderPipeline> identities
    ) throws Exception {
        RenderPipeline pipeline = (RenderPipeline) compileProgram.invoke(renderer, pass, stage);
        Variant key = new Variant(pass, stage, fullbright);
        RenderPipeline previous = variants.putIfAbsent(key, pipeline);
        require(previous == null || previous == pipeline,
                "Pipeline identity changed after a lighting toggle: " + key);
        require(pipeline.getShaderDefines().flags().contains(FULLBRIGHT_DEFINE) == fullbright,
                "Pipeline cached with the wrong fullbright define: " + key);
        require(pipeline.getShaderDefines().flags().contains("USE_FOG"),
                "Sodium's vertex/fragment fog interface must remain enabled: " + key);
        identities.add(pipeline);
    }

    private static void checkShaderSourceProvider() throws Exception {
        String source;
        try (InputStream stream = SodiumSmokeChecks.class.getResourceAsStream(
                "/assets/sodium/shaders/blocks/block_layer_opaque.vsh")) {
            require(stream != null, "Could not load the installed Sodium terrain shader");
            source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        String expected = ShaderSourceTransform.addFullbrightTerrainVariant(source);
        require(expected != source, "Installed Sodium terrain shader did not match the lightmap transform");

        Class<?> keyType = Class.forName("net.minecraft.client.renderer.ShaderManager$ShaderSourceKey");
        Constructor<?> keyConstructor = keyType.getDeclaredConstructor(Identifier.class, ShaderType.class);
        keyConstructor.setAccessible(true);
        Identifier terrain = Identifier.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque");
        Identifier otherPath = Identifier.fromNamespaceAndPath("sodium", "unrelated_shader");
        Identifier otherNamespace = Identifier.fromNamespaceAndPath("minecraft", "blocks/block_layer_opaque");
        Map<Object, String> sources = new HashMap<>();
        sources.put(keyConstructor.newInstance(terrain, ShaderType.VERTEX), source);
        sources.put(keyConstructor.newInstance(terrain, ShaderType.FRAGMENT), source);
        sources.put(keyConstructor.newInstance(otherPath, ShaderType.VERTEX), source);
        sources.put(keyConstructor.newInstance(otherNamespace, ShaderType.VERTEX), source);

        Class<?> configsType = Class.forName("net.minecraft.client.renderer.ShaderManager$Configs");
        Constructor<?> constructor = configsType.getDeclaredConstructor(Map.class, Map.class, Map.class);
        constructor.setAccessible(true);
        Object configs = constructor.newInstance(sources, Map.of(), Map.of());
        Method getShader = configsType.getDeclaredMethod("getShader", Identifier.class, ShaderType.class);
        getShader.setAccessible(true);
        require(expected.equals(getShader.invoke(configs, terrain, ShaderType.VERTEX)),
                "Transformed shader provider did not apply the fullbright terrain guard");
        require(getShader.invoke(configs, terrain, ShaderType.FRAGMENT) == source,
                "Shader transform must leave fragment sources unchanged");
        require(getShader.invoke(configs, otherPath, ShaderType.VERTEX) == source,
                "Shader transform must leave unrelated Sodium shaders unchanged");
        require(getShader.invoke(configs, otherNamespace, ShaderType.VERTEX) == source,
                "Shader transform must leave other namespaces unchanged");
        require(getShader.invoke(configs,
                        Identifier.fromNamespaceAndPath("sodium", "missing"), ShaderType.VERTEX) == null,
                "Shader transform must preserve missing-source behavior");
        System.out.println("Sodium terrain shader transformed through the real ShaderManager.Configs provider");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record Variant(Object pass, Object stage, boolean fullbright) {
    }
}
