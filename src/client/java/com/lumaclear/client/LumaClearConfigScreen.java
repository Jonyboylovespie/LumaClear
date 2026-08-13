package com.lumaclear.client;

import com.lumaclear.LumaClearConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;

/** Simple built-in configuration screen used by both Mod Menu and the F7 keybind. */
public final class LumaClearConfigScreen extends Screen {
    private static final int COLUMN_WIDTH = 150;
    private static final int FULL_WIDTH = 310;
    private static final LumaClearConfig.Preset[] SLIDER_PRESETS = {
            LumaClearConfig.Preset.FULLBRIGHT,
            LumaClearConfig.Preset.CLARITY
    };

    private final @Nullable Screen parent;

    public LumaClearConfigScreen(@Nullable Screen parent) {
        super(Component.literal("LumaClear Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = 38;

        this.addRenderableWidget(new PresetSlider(left, y));

        y += 25;
        this.addRenderableWidget(Button.builder(toggleLabel("Mod enabled", LumaClearConfig.active()), button -> {
            LumaClearClient.toggleLumaClear();
            this.refreshButtons();
        }).bounds(left, y, COLUMN_WIDTH, 20).build());
        this.addRenderableWidget(settingToggle(right, y, "Fullbright",
                () -> LumaClearConfig.get().fullbright,
                () -> LumaClearConfig.get().fullbright = !LumaClearConfig.get().fullbright));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable distance fog",
                () -> LumaClearConfig.get().noDistanceFog,
                () -> LumaClearConfig.get().noDistanceFog = !LumaClearConfig.get().noDistanceFog));
        this.addRenderableWidget(settingToggle(right, y, "Disable darkness fog",
                () -> LumaClearConfig.get().noDarknessFog,
                () -> LumaClearConfig.get().noDarknessFog = !LumaClearConfig.get().noDarknessFog));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable underwater fog",
                () -> LumaClearConfig.get().noUnderwaterFog,
                () -> LumaClearConfig.get().noUnderwaterFog = !LumaClearConfig.get().noUnderwaterFog));
        this.addRenderableWidget(settingToggle(right, y, "Disable lava fog",
                () -> LumaClearConfig.get().noLavaFog,
                () -> LumaClearConfig.get().noLavaFog = !LumaClearConfig.get().noLavaFog));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable powder snow fog",
                () -> LumaClearConfig.get().noPowderSnowFog,
                () -> LumaClearConfig.get().noPowderSnowFog = !LumaClearConfig.get().noPowderSnowFog));
        this.addRenderableWidget(settingToggle(right, y, "Disable Nether fog",
                () -> LumaClearConfig.get().noNetherFog,
                () -> LumaClearConfig.get().noNetherFog = !LumaClearConfig.get().noNetherFog));

        y += 21;
        this.addRenderableWidget(settingToggle(left, y, "Disable weather fog",
                () -> LumaClearConfig.get().noWeatherFog,
                () -> LumaClearConfig.get().noWeatherFog = !LumaClearConfig.get().noWeatherFog));
        this.addRenderableWidget(settingToggle(right, y, "Directional shading",
                () -> LumaClearConfig.get().directionalShading,
                () -> LumaClearConfig.get().directionalShading = !LumaClearConfig.get().directionalShading));

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(this.width / 2 - 75, this.height - 26, COLUMN_WIDTH, 20).build());
    }

    private Button settingToggle(int x, int y, String label, BooleanSupplier state, Runnable toggle) {
        return Button.builder(toggleLabel(label, state.getAsBoolean()), button -> {
            toggle.run();
            LumaClearConfig config = LumaClearConfig.get();
            config.enabled = true;
            config.preset = LumaClearConfig.Preset.CUSTOM;
            LumaClearClient.settingsChanged();
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
            super(x, y, FULL_WIDTH, 20, Component.empty(), presetValue(LumaClearConfig.get().preset));
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            LumaClearConfig.Preset activePreset = LumaClearConfig.get().preset;
            String name = activePreset == LumaClearConfig.Preset.CUSTOM
                    ? activePreset.displayName()
                    : SLIDER_PRESETS[presetIndex(this.value)].displayName();
            this.setMessage(Component.literal("Preset: " + name));
        }

        @Override
        protected void applyValue() {
            int index = presetIndex(this.value);
            this.value = (double) index / (SLIDER_PRESETS.length - 1);
            LumaClearConfig.Preset selected = SLIDER_PRESETS[index];
            if (LumaClearConfig.get().preset != selected) {
                LumaClearConfig.applyPreset(selected);
                LumaClearClient.settingsChanged();
                LumaClearConfigScreen.this.refreshButtons();
            }
        }
    }

    private static int presetIndex(double value) {
        return Math.max(0, Math.min(SLIDER_PRESETS.length - 1,
                (int) Math.round(value * (SLIDER_PRESETS.length - 1))));
    }

    private static double presetValue(LumaClearConfig.Preset preset) {
        for (int i = 0; i < SLIDER_PRESETS.length; i++) {
            if (SLIDER_PRESETS[i] == preset) {
                return (double) i / (SLIDER_PRESETS.length - 1);
            }
        }
        return (double) (SLIDER_PRESETS.length - 1) / (SLIDER_PRESETS.length - 1);
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
        LumaClearConfig.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}
