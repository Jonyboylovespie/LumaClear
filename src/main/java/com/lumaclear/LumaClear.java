package com.lumaclear;

import net.fabricmc.api.ModInitializer;

/** Common entry point kept intentionally empty: LumaClear is a client-only mod. */
public final class LumaClear implements ModInitializer {
    public static final String MOD_ID = "lumaclear";

    @Override
    public void onInitialize() {
        // All behavior is client-side and registered from LumaClearClient.
    }
}
