package com.lumaclear.smoke;

import com.lumaclear.LumaClearConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/** Runs real transformed light sampling and AO paths using a CPU-only test world. */
public final class LightingSmokeChecks {
    private LightingSmokeChecks() {
    }

    public static void run() throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        LumaClearConfig config = LumaClearConfig.get();
        boolean enabled = config.enabled;
        boolean fullbright = config.fullbright;
        try {
            config.enabled = true;
            config.fullbright = true;
            checkVanilla();
            checkVanillaAo(config);
            checkLightOnlyInvalidation(config);
            checkPacketLightInvalidation(config);
            if (FabricLoader.getInstance().isModLoaded("sodium")) {
                SodiumChecks.run(config);
            }
        } finally {
            config.enabled = enabled;
            config.fullbright = fullbright;
        }
        System.out.println("LUMACLEAR_LIGHTING_FAST_PATH_SMOKE_PASSED");
    }

    private static void checkVanilla() throws Exception {
        BlockModelLighter.Cache cache = new BlockModelLighter.Cache();
        require(cache.getLightCoords(null, null, null) == LightCoordsUtil.FULL_BRIGHT,
                "Vanilla/Indigo cache must return before accessing world lighting");
        FluidRenderer fluid = new FluidRenderer(null);
        Method light = FluidRenderer.class.getDeclaredMethod("getLightCoords", BlockAndTintGetter.class, BlockPos.class);
        light.setAccessible(true);
        require((Integer) light.invoke(fluid, null, null) == LightCoordsUtil.FULL_BRIGHT,
                "Fluid fullbright must not query neighboring light");
    }

    private static void checkLightOnlyInvalidation(LumaClearConfig config) throws Exception {
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeType.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
        ClientChunkCache cache = (ClientChunkCache) allocate.invoke(unsafe, ClientChunkCache.class);
        TrackingExtractor extractor = (TrackingExtractor) allocate.invoke(unsafe, TrackingExtractor.class);
        net.minecraft.client.Minecraft minecraft = (net.minecraft.client.Minecraft)
                allocate.invoke(unsafe, net.minecraft.client.Minecraft.class);
        Field extractorField = net.minecraft.client.Minecraft.class.getDeclaredField("levelExtractor");
        extractorField.setAccessible(true);
        extractorField.set(minecraft, extractor);
        Field instance = net.minecraft.client.Minecraft.class.getDeclaredField("instance");
        instance.setAccessible(true);
        Object previous = instance.get(null);
        try {
            instance.set(null, minecraft);
            for (boolean enabled : new boolean[]{false, true}) {
                for (boolean fullbright : new boolean[]{false, true}) {
                    config.enabled = enabled;
                    config.fullbright = fullbright;
                    int before = extractor.updates;
                    cache.onLightUpdate(LightLayer.BLOCK, net.minecraft.core.SectionPos.of(1, 2, 3));
                    require(extractor.updates == before + (enabled && fullbright ? 0 : 1),
                            "Only active fullbright can suppress a lighting-only render notification");
                    extractor.setSectionDirty(1, 2, 3);
                    require(extractor.updates == before + (enabled && fullbright ? 1 : 2),
                            "Ordinary render invalidations must remain active");
                }
            }
        } finally {
            instance.set(null, previous);
        }
    }

    private static final class TrackingExtractor extends LevelExtractor {
        private int updates;
        private TrackingExtractor() { super(null, null, null); }
        @Override public void setSectionDirty(int x, int y, int z) { updates++; }
    }

    private static void checkPacketLightInvalidation(LumaClearConfig config) throws Exception {
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeType.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
        var listener = (net.minecraft.client.multiplayer.ClientPacketListener) allocate.invoke(unsafe,
                net.minecraft.client.multiplayer.ClientPacketListener.class);
        TrackingLevel level = (TrackingLevel) allocate.invoke(unsafe, TrackingLevel.class);
        TrackingLightEngine engine = (TrackingLightEngine) allocate.invoke(unsafe, TrackingLightEngine.class);
        Field world = listener.getClass().getDeclaredField("level");
        world.setAccessible(true);
        world.set(listener, level);
        Method read = listener.getClass().getDeclaredMethod("readSectionList", int.class, int.class,
                net.minecraft.world.level.lighting.LevelLightEngine.class, LightLayer.class,
                java.util.BitSet.class, java.util.BitSet.class, java.util.Iterator.class, boolean.class);
        read.setAccessible(true);
        java.util.BitSet dataMask = new java.util.BitSet();
        dataMask.set(1);
        java.util.BitSet emptyMask = new java.util.BitSet();
        emptyMask.set(2);
        for (boolean enabled : new boolean[]{false, true}) {
            for (boolean fullbright : new boolean[]{false, true}) {
                config.enabled = enabled;
                config.fullbright = fullbright;
                level.updates = 0;
                engine.queued = 0;
                read.invoke(listener, 3, 4, engine, LightLayer.SKY, dataMask, emptyMask,
                        java.util.List.of(new byte[2048]).iterator(), true);
                require(engine.queued == 2, "Light packet data must always reach world light storage");
                require(level.updates == (enabled && fullbright ? 0 : 2),
                        "Only fullbright can skip packet-induced light-only rebuilds");
            }
        }
    }

    private static final class TrackingLevel extends net.minecraft.client.multiplayer.ClientLevel {
        private int updates;
        private TrackingLevel() { super(null, null, null, null, 0, 0, null, false, 0, 0); }
        @Override public void setSectionDirtyWithNeighbors(int x, int y, int z) { updates++; }
    }

    private static final class TrackingLightEngine extends net.minecraft.world.level.lighting.LevelLightEngine {
        private int queued;
        private TrackingLightEngine() { super(null, false, false); }
        @Override public int getLightSectionCount() { return 3; }
        @Override public int getMinLightSection() { return -2; }
        @Override public void queueSectionData(LightLayer layer, net.minecraft.core.SectionPos pos,
                net.minecraft.world.level.chunk.DataLayer data) {
            require(data != null, "Packet lighting storage must retain non-null section data");
            queued++;
        }
    }

    private static void checkVanillaAo(LumaClearConfig config) {
        int[] reads = {0};
        BlockAndTintGetter level = world(Blocks.GLASS.defaultBlockState(), reads, true);
        BlockModelLighter lighter = new BlockModelLighter();
        BakedQuad.MaterialInfo material = new BakedQuad.MaterialInfo(null, null, null, null, null, -1, null, 0);
        BakedQuad quad = new BakedQuad(new Vector3f(0, 1, 0), new Vector3f(0, 1, 1),
                new Vector3f(1, 1, 1), new Vector3f(1, 1, 0), 0, 0, 0, 0, Direction.UP, material);
        QuadInstance normal = new QuadInstance();
        QuadInstance bright = new QuadInstance();
        config.enabled = true;
        config.fullbright = false;
        lighter.prepareQuadAmbientOcclusion(level, Blocks.STONE.defaultBlockState(), BlockPos.ZERO, quad, normal);
        require(reads[0] > 0, "Vanilla reference did not sample lighting");
        reads[0] = 0;
        config.fullbright = true;
        lighter.prepareQuadAmbientOcclusion(level, Blocks.STONE.defaultBlockState(), BlockPos.ZERO, quad, bright);
        require(reads[0] == 0, "Vanilla AO still queried sky/block light in fullbright");
        boolean shaded = false;
        for (int i = 0; i < 4; i++) {
            require(normal.getColor(i) == bright.getColor(i), "Vanilla corner AO changed");
            shaded |= net.minecraft.util.ARGB.red(bright.getColor(i)) < 255;
        }
        require(shaded, "AO fixture must contain real corner shading");
    }

    private static final class SodiumChecks {
        private static void run(LumaClearConfig config) throws Exception {
            for (BlockState state : new BlockState[]{Blocks.AIR.defaultBlockState(), Blocks.STONE.defaultBlockState(),
                    Blocks.WATER.defaultBlockState(), Blocks.GLASS.defaultBlockState(), Blocks.GLOWSTONE.defaultBlockState(),
                    Blocks.MAGMA_BLOCK.defaultBlockState(), Blocks.LAVA.defaultBlockState()}) {
                int[] reads = {0};
                BlockAndTintGetter sampleLevel = world(state, reads, false);
                net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess cache =
                        new net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess() {
                            { this.level = sampleLevel; }
                            @Override public int get(int x, int y, int z) { return compute(x, y, z); }
                        };
                config.enabled = false;
                int normal = cache.get(0, 0, 0);
                int normalReads = reads[0];
                config.enabled = true;
                config.fullbright = true;
                reads[0] = 0;
                int bright = cache.get(0, 0, 0);
                require(reads[0] == 0, "Sodium fullbright still read sky/block light: " + state);
                require((normal & ~255) == (bright & ~255), "Sodium AO/shape/emission data changed: " + state);
                config.fullbright = false;
                reads[0] = 0;
                require(cache.get(0, 0, 0) == normal && reads[0] == normalReads,
                        "Disabling fullbright must restore normal light sampling");
                // Feed the real normal/fullbright cache through Sodium's smooth lighter.
                net.caffeinemc.mods.sodium.client.model.quad.ModelQuad quad =
                        new net.caffeinemc.mods.sodium.client.model.quad.ModelQuad();
                quad.setFlags(4);
                quad.setLightFace(Direction.UP);
                float[][] brightness = new float[2][];
                for (int i = 0; i < 2; i++) {
                    config.fullbright = i == 1;
                    var out = new net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData();
                    new net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline(cache)
                            .calculate(quad, BlockPos.ZERO, out, Direction.UP, Direction.UP, null, false);
                    brightness[i] = out.br;
                }
                require(Arrays.equals(brightness[0], brightness[1]), "Smooth AO changed with fullbright: " + state);
            }
        }

    }

    private static BlockAndTintGetter world(BlockState state, int[] lightReads, boolean mixed) {
        return (BlockAndTintGetter) Proxy.newProxyInstance(LightingSmokeChecks.class.getClassLoader(),
                new Class<?>[]{BlockAndTintGetter.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getBlockState" -> {
                        BlockPos pos = (BlockPos) args[0];
                        yield mixed && (pos.getX() + 2 * pos.getZ()) % 3 == 0 ? Blocks.STONE.defaultBlockState() : state;
                    }
                    case "getFluidState" -> state.getFluidState();
                    case "getBrightness" -> { lightReads[0]++; yield args[0] == LightLayer.SKY ? 9 : 3; }
                    case "cardinalLighting" -> new net.minecraft.world.level.CardinalLighting(0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F);
                    case "getMinY" -> -64;
                    case "getHeight" -> 384;
                    case "getBlockEntity" -> null;
                    default -> throw new AssertionError("Unexpected world access: " + method);
                });
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
