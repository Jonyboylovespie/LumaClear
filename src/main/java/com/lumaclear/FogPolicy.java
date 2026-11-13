package com.lumaclear;

/** Pure decisions shared by vanilla fog providers and regression tests. A zero mask preserves vanilla behavior. */
public final class FogPolicy {
    public static final int DISTANCE = 1;
    public static final int DARKNESS = 1 << 1;
    public static final int WATER = 1 << 2;
    public static final int LAVA = 1 << 3;
    public static final int POWDER_SNOW = 1 << 4;
    public static final int NETHER = 1 << 5;
    public static final int WEATHER = 1 << 6;

    public enum Environment {
        LAVA, POWDER_SNOW, BLINDNESS, DARKNESS, WATER, ATMOSPHERIC, OTHER
    }

    private FogPolicy() {
    }

    public static boolean removed(Environment environment, int disabled) {
        int flag = switch (environment) {
            case LAVA -> LAVA;
            case POWDER_SNOW -> POWDER_SNOW;
            case BLINDNESS, DARKNESS -> DARKNESS;
            case WATER -> WATER;
            case ATMOSPHERIC, OTHER -> 0;
        };
        return (disabled & flag) != 0;
    }

    /** Retains vanilla provider priority, allowing ordinary atmospheric fog after an obstructing medium is removed. */
    public static boolean applicable(
            Environment environment, boolean vanillaApplicable, boolean mediumRemoved, int disabled
    ) {
        return !removed(environment, disabled)
                && (vanillaApplicable || (environment == Environment.ATMOSPHERIC && mediumRemoved));
    }

    /** Nether fog has its own switch even when ordinary distance fog is removed. */
    public static boolean atmosphericBaseRemoved(boolean nether, int disabled) {
        return (disabled & (nether ? NETHER : DISTANCE)) != 0;
    }

    public static float atmosphericEnd(float attributeEnd, float optionDistance, boolean baseRemoved, int disabled) {
        float environmentEnd = baseRemoved ? Float.MAX_VALUE : attributeEnd;
        return (disabled & DISTANCE) != 0 ? environmentEnd : Math.min(optionDistance, environmentEnd);
    }

    /** Moves the rain band beyond the visible world as rain fades, including vanilla's asymptotic tail. */
    public static float rainFogStart(float renderDistance, float rainMultiplier) {
        float end = rainFogEnd(renderDistance, rainMultiplier);
        if (end == Float.MAX_VALUE) {
            return Float.MAX_VALUE;
        }
        float start = Math.max(0.0F, end - 160.0F);
        return start < end ? start : Math.nextDown(end);
    }

    /** Removing a biome/dimension baseline must not leave a permanent distance cap after rain stops. */
    public static float rainFogEnd(float renderDistance, float rainMultiplier) {
        if (!(rainMultiplier > 0.0F)) {
            return Float.MAX_VALUE;
        }
        float stormEnd = Math.max(Math.min(96.0F, renderDistance), renderDistance - 256.0F);
        double end = (double) stormEnd / rainMultiplier;
        return end >= Float.MAX_VALUE ? Float.MAX_VALUE : (float) end;
    }
}
