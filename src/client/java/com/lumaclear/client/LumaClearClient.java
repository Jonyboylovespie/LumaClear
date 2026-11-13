package com.lumaclear.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.lumaclear.LumaClearConfig;
import com.lumaclear.RenderSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class LumaClearClient implements ClientModInitializer {
    private static KeyMapping configKey;
    private static RenderSettings appliedSettings;

    @Override
    public void onInitializeClient() {
        LumaClearConfig.load();
        appliedSettings = currentRenderSettings();

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lumaclear.config", InputConstants.Type.KEYBOARD, InputConstants.KEY_F7,
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
        config.enabled = !config.enabled;
        settingsChanged();
    }

    public static void settingsChanged() {
        LumaClearConfig.save();
        RenderSettings next = currentRenderSettings();
        if (appliedSettings == null || appliedSettings.needsLightmapUpdate(next)) {
            LumaClearConfig.markRenderStateDirty();
        }
        if (appliedSettings != null && appliedSettings.needsGeometryRebuild(next)) {
            rebuildWorldGeometry();
        }
        appliedSettings = next;
    }

    private static RenderSettings currentRenderSettings() {
        return new RenderSettings(LumaClearConfig.fullbrightActive(),
                !LumaClearConfig.active() || LumaClearConfig.get().directionalShading);
    }

    /** Baked directional brightness and fullbright sampling policy require fresh chunk vertices. */
    private static void rebuildWorldGeometry() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.level != null) {
            client.levelRenderer.invalidateCompiledGeometry(
                    client.level,
                    client.options,
                    client.gameRenderer.mainCamera(),
                    client.getBlockColors()
            );
        }
    }
}
