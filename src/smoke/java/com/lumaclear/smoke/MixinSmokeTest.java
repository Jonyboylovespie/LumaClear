package com.lumaclear.smoke;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/** Forces target transformation before Minecraft opens a window or initializes GPU resources. */
public final class MixinSmokeTest implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        try {
            List<String> targets = List.of(
                    "net.minecraft.client.renderer.LightmapRenderStateExtractor",
                    "net.minecraft.client.renderer.fog.FogRenderer",
                    "net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment",
                    "net.minecraft.client.renderer.LevelRenderer",
                    "net.minecraft.client.renderer.block.BlockModelLighter",
                    "net.minecraft.client.renderer.block.BlockModelLighter$Cache",
                    "net.minecraft.client.multiplayer.ClientChunkCache",
                    "net.minecraft.client.multiplayer.ClientPacketListener",
                    "net.minecraft.client.renderer.block.FluidRenderer",
                    "net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator",
                    "net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.FlatLighter"
            );
            for (String target : targets) {
                checkTransformed(target);
            }
            if (FabricLoader.getInstance().isModLoaded("sodium")) {
                for (String target : List.of(
                        "net.minecraft.client.renderer.ShaderManager$Configs",
                        "net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer",
                        "net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline",
                        "net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline",
                        "net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline",
                        "net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess"
                )) {
                    checkTransformed(target);
                }
                SodiumSmokeChecks.run();
                checkFogCaptureOrder();
            }
            DirectionalSmokeChecks.run();
            FogSmokeChecks.run();
            SettingsSmokeChecks.run();
            LightingSmokeChecks.run();
            System.out.println("LUMACLEAR_MIXIN_SMOKE_PASSED");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void checkFogCaptureOrder() throws Exception {
        ClassNode fogRenderer = new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(
                ".mixin.out/class/net/minecraft/client/renderer/fog/FogRenderer.class")))
                .accept(fogRenderer, 0);
        for (var method : fogRenderer.methods) {
            if (!method.name.equals("setupFog")) continue;
            int adjustment = -1;
            int capture = -1;
            int position = 0;
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode invoke) {
                    if (invoke.name.contains("lumaclear$applyFog")) adjustment = position;
                    if (invoke.name.contains("sodium$storeFogParameters")) capture = position;
                }
                position++;
            }
            if (adjustment < 0 || capture < 0 || adjustment >= capture) {
                throw new AssertionError("Sodium must capture fog after LumaClear adjusts it: adjustment="
                        + adjustment + ", capture=" + capture);
            }
            System.out.println("LumaClear fog adjustment runs before Sodium's culling capture");
            return;
        }
        throw new AssertionError("Missing transformed FogRenderer.setupFog");
    }

    private static void checkTransformed(String target) throws ClassNotFoundException {
        Class<?> type = Class.forName(target, false, Thread.currentThread().getContextClassLoader());
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().contains("lumaclear$")) {
                System.out.println("LumaClear mixin applied: " + target);
                return;
            }
        }
        throw new AssertionError("LumaClear mixin missing from " + target);
    }
}
