package com.lumenless;

import net.fabricmc.api.ModInitializer;

/** Common entry point kept intentionally empty: Lumenless is a client-only mod. */
public final class Lumenless implements ModInitializer {
    public static final String MOD_ID = "lumenless";

    @Override
    public void onInitialize() {
        // All behavior is client-side and registered from LumenlessClient.
    }
}
