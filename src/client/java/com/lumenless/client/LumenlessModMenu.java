package com.lumenless.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Adds Lumenless's built-in settings screen to the optional Mod Menu configuration button. */
public final class LumenlessModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LumenlessConfigScreen::new;
    }
}
