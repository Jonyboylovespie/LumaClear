package com.lumenless.client;

import com.lumenless.LumenlessConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;

/** Simple built-in configuration screen used by both Mod Menu and the F7 keybind. */
public final class LumenlessConfigScreen extends Screen {
    private static final int COLUMN_WIDTH = 150;
    private static final int FULL_WIDTH = 310;
    private static final LumenlessConfig.Preset[] SLIDER_PRESETS = {
            LumenlessConfig.Preset.VANILLA,
            LumenlessConfig.Preset.FULLBRIGHT,
            LumenlessConfig.Preset.CLARITY,
            LumenlessConfig.Preset.MAXIMUM
    };

    private final @Nullable Screen parent;

    public LumenlessConfigScreen(@Nullable Screen parent) {
        super(Component.literal("Lumenless Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = 38;

        this.addRenderableWidget(new PresetSlider(left, y));

        y += 25;
        this.addRenderableWidget(Button.builder(toggleLabel("Mod enabled", LumenlessConfig.active()), button -> {
            LumenlessClient.toggleLumenless();
            this.refreshButtons();
        }).bounds(left, y, COLUMN_WIDTH, 20).build());
        this.addRenderableWidget(settingToggle(right, y, "Fullbright",
                () -> LumenlessConfig.get().fullbright,
                () -> LumenlessConfig.get().fullbright = !LumenlessConfig.get().fullbright));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable distance fog",
                () -> LumenlessConfig.get().noDistanceFog,
                () -> LumenlessConfig.get().noDistanceFog = !LumenlessConfig.get().noDistanceFog));
        this.addRenderableWidget(settingToggle(right, y, "Disable darkness fog",
                () -> LumenlessConfig.get().noDarknessFog,
                () -> LumenlessConfig.get().noDarknessFog = !LumenlessConfig.get().noDarknessFog));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable underwater fog",
                () -> LumenlessConfig.get().noUnderwaterFog,
                () -> LumenlessConfig.get().noUnderwaterFog = !LumenlessConfig.get().noUnderwaterFog));
        this.addRenderableWidget(settingToggle(right, y, "Disable lava fog",
                () -> LumenlessConfig.get().noLavaFog,
                () -> LumenlessConfig.get().noLavaFog = !LumenlessConfig.get().noLavaFog));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable powder snow fog",
                () -> LumenlessConfig.get().noPowderSnowFog,
                () -> LumenlessConfig.get().noPowderSnowFog = !LumenlessConfig.get().noPowderSnowFog));
        this.addRenderableWidget(settingToggle(right, y, "Disable Nether fog",
                () -> LumenlessConfig.get().noNetherFog,
                () -> LumenlessConfig.get().noNetherFog = !LumenlessConfig.get().noNetherFog));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable weather fog",
                () -> LumenlessConfig.get().noWeatherFog,
                () -> LumenlessConfig.get().noWeatherFog = !LumenlessConfig.get().noWeatherFog));
        this.addRenderableWidget(settingToggle(right, y, "Disable ambient occlusion",
                () -> LumenlessConfig.get().disableAmbientOcclusion,
                () -> LumenlessConfig.get().disableAmbientOcclusion = !LumenlessConfig.get().disableAmbientOcclusion));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Directional shading",
                () -> LumenlessConfig.get().directionalShading,
                () -> LumenlessConfig.get().directionalShading = !LumenlessConfig.get().directionalShading));
        this.addRenderableWidget(settingToggle(right, y, "Hide vignette",
                () -> LumenlessConfig.get().hideVignette,
                () -> LumenlessConfig.get().hideVignette = !LumenlessConfig.get().hideVignette));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Hide rain",
                () -> LumenlessConfig.get().hideRain,
                () -> LumenlessConfig.get().hideRain = !LumenlessConfig.get().hideRain));
        this.addRenderableWidget(settingToggle(right, y, "Hide snow",
                () -> LumenlessConfig.get().hideSnow,
                () -> LumenlessConfig.get().hideSnow = !LumenlessConfig.get().hideSnow));

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(this.width / 2 - 75, this.height - 26, COLUMN_WIDTH, 20).build());
    }

    private Button settingToggle(int x, int y, String label, BooleanSupplier state, Runnable toggle) {
        return Button.builder(toggleLabel(label, state.getAsBoolean()), button -> {
            toggle.run();
            LumenlessConfig config = LumenlessConfig.get();
            config.enabled = true;
            config.preset = LumenlessConfig.Preset.CUSTOM;
            LumenlessClient.settingsChanged();
            this.refreshButtons();
        }).bounds(x, y, COLUMN_WIDTH, 20).build();
    }

    private void refreshButtons() {
        this.clearWidgets();
        this.init();
    }

    private static Component toggleLabel(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"));
    }

    private final class PresetSlider extends AbstractSliderButton {
        private PresetSlider(int x, int y) {
            super(x, y, FULL_WIDTH, 20, Component.empty(), presetValue(LumenlessConfig.get().preset));
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            LumenlessConfig.Preset activePreset = LumenlessConfig.get().preset;
            String name = activePreset == LumenlessConfig.Preset.CUSTOM
                    ? activePreset.displayName()
                    : SLIDER_PRESETS[presetIndex(this.value)].displayName();
            this.setMessage(Component.literal("Preset: " + name));
        }

        @Override
        protected void applyValue() {
            int index = presetIndex(this.value);
            this.value = (double) index / (SLIDER_PRESETS.length - 1);
            LumenlessConfig.Preset selected = SLIDER_PRESETS[index];
            if (LumenlessConfig.get().preset != selected) {
                LumenlessConfig.applyPreset(selected);
                LumenlessClient.settingsChanged();
                LumenlessConfigScreen.this.refreshButtons();
            }
        }
    }

    private static int presetIndex(double value) {
        return Math.max(0, Math.min(SLIDER_PRESETS.length - 1,
                (int) Math.round(value * (SLIDER_PRESETS.length - 1))));
    }

    private static double presetValue(LumenlessConfig.Preset preset) {
        for (int i = 0; i < SLIDER_PRESETS.length; i++) {
            if (SLIDER_PRESETS[i] == preset) {
                return (double) i / (SLIDER_PRESETS.length - 1);
            }
        }
        return (double) 2 / (SLIDER_PRESETS.length - 1);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xD0101010);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(this.font, "Default: Clarity • changes apply immediately", this.width / 2, 24, 0xFFB0B0B0);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        LumenlessConfig.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}
