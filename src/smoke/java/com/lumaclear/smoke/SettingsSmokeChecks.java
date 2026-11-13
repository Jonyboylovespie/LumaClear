package com.lumaclear.smoke;

import com.lumaclear.LumaClearConfig;
import com.lumaclear.RenderSettings;
import com.lumaclear.client.LumaClearClient;
import com.lumaclear.client.LumaClearConfigScreen;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.input.KeyEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Checks real settings callbacks and keyboard activation without a Minecraft instance. */
public final class SettingsSmokeChecks {
    private SettingsSmokeChecks() {
    }

    public static void run() throws ReflectiveOperationException {
        LumaClearConfig config = LumaClearConfig.get();
        LumaClearConfig.applyPreset(LumaClearConfig.Preset.CLARITY);
        Field applied = LumaClearClient.class.getDeclaredField("appliedSettings");
        applied.setAccessible(true);
        applied.set(null, new RenderSettings(true, true));
        LumaClearConfig.consumeRenderStateDirty();

        // Geometry invalidation is safe before a Minecraft instance exists.
        config.noWeatherFog = false;
        LumaClearClient.settingsChanged();
        require(!LumaClearConfig.consumeRenderStateDirty(), "Weather changes must not update the lightmap");
        config.fullbright = false;
        LumaClearClient.settingsChanged();
        require(LumaClearConfig.consumeRenderStateDirty(), "Fullbright changes must update the lightmap");

        LumaClearConfig.applyPreset(LumaClearConfig.Preset.FULLBRIGHT);
        Method create = LumaClearConfigScreen.class.getDeclaredMethod(
                "createPresetButton", int.class, int.class, Runnable.class);
        create.setAccessible(true);
        int[] refreshes = {0};
        @SuppressWarnings("unchecked")
        CycleButton<LumaClearConfig.Preset> button = (CycleButton<LumaClearConfig.Preset>)
                create.invoke(null, 0, 0, (Runnable) () -> refreshes[0]++);

        // These are the same activation callbacks used by AbstractButton's
        // Enter/Space handling; invoking them directly avoids its sound device.
        button.onPress(new KeyEvent(257, 0, 0));
        require(config.preset == LumaClearConfig.Preset.CLARITY && config.noUnderwaterFog,
                "Enter activation must select and apply Clarity");
        button.onPress(new KeyEvent(32, 0, 0));
        require(config.preset == LumaClearConfig.Preset.CUSTOM && config.noUnderwaterFog,
                "Space activation must select Custom while retaining individual settings");
        button.onPress(new KeyEvent(257, 0, 0));
        require(config.preset == LumaClearConfig.Preset.FULLBRIGHT && !config.noUnderwaterFog,
                "Preset cycling must wrap and apply Fullbright");
        button.onPress(new KeyEvent(257, 0, 1));
        require(config.preset == LumaClearConfig.Preset.CUSTOM,
                "Shift activation must cycle backwards");
        require(refreshes[0] == 4, "Each activation must refresh the labels once");
        require(button.getValue() == config.preset, "Widget and persisted preset must agree");
        System.out.println("LUMACLEAR_SETTINGS_BEHAVIOR_SMOKE_PASSED");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
