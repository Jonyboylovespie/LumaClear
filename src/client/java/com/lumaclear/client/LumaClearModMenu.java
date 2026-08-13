package com.lumaclear.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Adds LumaClear's built-in settings screen to the optional Mod Menu configuration button. */
public final class LumaClearModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LumaClearConfigScreen::new;
    }
}
