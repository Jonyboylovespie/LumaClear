package com.lumaclear.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.lumaclear.LumaClearConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Map;

public final class LumaClearClient implements ClientModInitializer {
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        LumaClearConfig.load();

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lumaclear.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7,
                KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(LumaClearClient::handleKeys);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> LumaClearConfig.save());
    }

    private static void handleKeys(Minecraft client) {
        while (configKey.consumeClick()) {
            client.gui.setScreen(new LumaClearConfigScreen(client.gui.screen()));
        }
    }

    public static void toggleLumaClear() {
        LumaClearConfig config = LumaClearConfig.get();
        if (config.enabled) {
            config.enabled = false;
        } else {
            config.enabled = true;
        }
        settingsChanged();
    }

    public static void settingsChanged() {
        LumaClearConfig.save();
        LumaClearConfig.markRenderStateDirty();
        invalidateSodiumPrograms();
        rebuildWorldGeometry();
    }

    /**
     * Block lighting is baked into compiled chunk geometry. Rebuild it when a preset changes so already-rendered
     * chunks do not retain stale LumaClear light/AO values.
     */
    private static void rebuildWorldGeometry() {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            client.levelRenderer.invalidateCompiledGeometry(
                    client.level,
                    client.options,
                    client.gameRenderer.mainCamera(),
                    client.getBlockColors()
            );
        }
    }

    /** Sodium caches terrain programs by render pass; clear that optional cache after changing fog defines. */
    private static void invalidateSodiumPrograms() {
        if (!FabricLoader.getInstance().isModLoaded("sodium")) {
            return;
        }

        try {
            Class<?> renderer = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer");
            Field field = renderer.getDeclaredField("programs");
            field.setAccessible(true);
            Object value = field.get(null);
            if (value instanceof Map<?, ?> programs) {
                programs.clear();
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // The integration is optional and Sodium may change its internal cache in a future release.
        }
    }
}
