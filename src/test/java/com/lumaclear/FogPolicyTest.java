package com.lumaclear;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.stream.Stream;

import static com.lumaclear.FogPolicy.Environment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FogPolicyTest {
    private record SelectionCase(
            String name, EnumSet<Environment> vanillaApplicable, Environment medium, int disabled,
            Environment expected
    ) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static Stream<SelectionCase> providerCases() {
        return Stream.of(
                new SelectionCase("disabled mod preserves lava before blindness",
                        EnumSet.of(Environment.LAVA, Environment.BLINDNESS), Environment.LAVA, 0, Environment.LAVA),
                new SelectionCase("darkness switch must not remove retained lava",
                        EnumSet.of(Environment.LAVA, Environment.BLINDNESS), Environment.LAVA,
                        FogPolicy.DARKNESS, Environment.LAVA),
                new SelectionCase("removing lava falls back to retained blindness",
                        EnumSet.of(Environment.LAVA, Environment.BLINDNESS), Environment.LAVA,
                        FogPolicy.LAVA, Environment.BLINDNESS),
                new SelectionCase("removing lava and blindness falls back to atmosphere",
                        EnumSet.of(Environment.LAVA, Environment.BLINDNESS), Environment.LAVA,
                        FogPolicy.LAVA | FogPolicy.DARKNESS, Environment.ATMOSPHERIC),
                new SelectionCase("darkness switch must not remove retained powder snow",
                        EnumSet.of(Environment.POWDER_SNOW, Environment.DARKNESS), Environment.POWDER_SNOW,
                        FogPolicy.DARKNESS, Environment.POWDER_SNOW),
                new SelectionCase("removing powder snow preserves darkness",
                        EnumSet.of(Environment.POWDER_SNOW, Environment.DARKNESS), Environment.POWDER_SNOW,
                        FogPolicy.POWDER_SNOW, Environment.DARKNESS),
                new SelectionCase("removing water preserves blindness priority",
                        EnumSet.of(Environment.BLINDNESS, Environment.WATER), Environment.WATER,
                        FogPolicy.WATER, Environment.BLINDNESS),
                new SelectionCase("removing darkness restores underwater fog",
                        EnumSet.of(Environment.DARKNESS, Environment.WATER), Environment.WATER,
                        FogPolicy.DARKNESS, Environment.WATER),
                new SelectionCase("removing water and darkness falls back to atmosphere",
                        EnumSet.of(Environment.DARKNESS, Environment.WATER), Environment.WATER,
                        FogPolicy.DARKNESS | FogPolicy.WATER, Environment.ATMOSPHERIC),
                new SelectionCase("Nether clearing preserves blindness",
                        EnumSet.of(Environment.BLINDNESS, Environment.ATMOSPHERIC), Environment.ATMOSPHERIC,
                        FogPolicy.NETHER, Environment.BLINDNESS),
                new SelectionCase("distance clearing preserves darkness",
                        EnumSet.of(Environment.DARKNESS, Environment.ATMOSPHERIC), Environment.ATMOSPHERIC,
                        FogPolicy.DISTANCE, Environment.DARKNESS),
                new SelectionCase("removing darkness restores normal atmospheric fog",
                        EnumSet.of(Environment.DARKNESS, Environment.ATMOSPHERIC), Environment.ATMOSPHERIC,
                        FogPolicy.DARKNESS, Environment.ATMOSPHERIC),
                new SelectionCase("removing water retains dimension and weather provider",
                        EnumSet.of(Environment.WATER), Environment.WATER, FogPolicy.WATER, Environment.ATMOSPHERIC)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providerCases")
    void customFogSettingsPreserveUnrelatedProviders(SelectionCase testCase) {
        boolean mediumRemoved = FogPolicy.removed(testCase.medium, testCase.disabled);
        Environment selected = Stream.of(Environment.LAVA, Environment.POWDER_SNOW, Environment.BLINDNESS,
                        Environment.DARKNESS, Environment.WATER, Environment.ATMOSPHERIC)
                .filter(environment -> FogPolicy.applicable(environment,
                        testCase.vanillaApplicable.contains(environment), mediumRemoved, testCase.disabled))
                .findFirst().orElseThrow();
        assertEquals(testCase.expected, selected);
    }

    @Test
    void distanceAndNetherBaselineSettingsAreIndependent() {
        assertFalse(FogPolicy.atmosphericBaseRemoved(true, FogPolicy.DISTANCE));
        assertTrue(FogPolicy.atmosphericBaseRemoved(false, FogPolicy.DISTANCE));
        assertTrue(FogPolicy.atmosphericBaseRemoved(true, FogPolicy.NETHER));
        assertFalse(FogPolicy.atmosphericBaseRemoved(false, FogPolicy.NETHER));
        assertFalse(FogPolicy.atmosphericBaseRemoved(false, 0));
    }

    @Test
    void skyAndCloudEndsRetainEnvironmentCapsIndependentlyOfDistanceCaps() {
        assertEquals(32.0F, FogPolicy.atmosphericEnd(32.0F, 512.0F, false, FogPolicy.DISTANCE));
        assertEquals(512.0F, FogPolicy.atmosphericEnd(32.0F, 512.0F, true, FogPolicy.NETHER));
        assertEquals(Float.MAX_VALUE, FogPolicy.atmosphericEnd(32.0F, 512.0F, true, FogPolicy.DISTANCE));
        assertEquals(128.0F, FogPolicy.atmosphericEnd(512.0F, 128.0F, false, 0));
    }

    @Test
    void removingTheAtmosphericBaselinePreservesEnabledRainFog() {
        assertEquals(Float.MAX_VALUE, FogPolicy.rainFogStart(512.0F, 0.0F));
        assertEquals(Float.MAX_VALUE, FogPolicy.rainFogEnd(512.0F, 0.0F));
        assertEquals(352.0F, FogPolicy.rainFogStart(512.0F, 0.5F));
        assertEquals(512.0F, FogPolicy.rainFogEnd(512.0F, 0.5F));
        assertEquals(96.0F, FogPolicy.rainFogEnd(128.0F, 1.0F));
        assertEquals(64.0F, FogPolicy.rainFogEnd(64.0F, 1.0F));
        for (float distance : new float[] {32.0F, 128.0F, 512.0F, 1024.0F}) {
            for (float rain : new float[] {1.0E-8F, 0.001F, 0.25F, 0.5F, 1.0F}) {
                assertTrue(FogPolicy.rainFogStart(distance, rain) < FogPolicy.rainFogEnd(distance, rain),
                        "Rain fog must have a positive span at distance " + distance + " and rain " + rain);
            }
        }
    }

    @Test
    void rainFogFadesAwayDuringVanillasAsymptoticWeatherTail() {
        for (float distance : new float[] {32.0F, 128.0F, 512.0F, 1024.0F}) {
            float previousEnd = 0.0F;
            for (float rain : new float[] {1.0F, 0.5F, 0.01F, 1.0E-8F, Float.MIN_VALUE, 0.0F}) {
                float start = FogPolicy.rainFogStart(distance, rain);
                float end = FogPolicy.rainFogEnd(distance, rain);
                assertTrue(Float.isFinite(start) && Float.isFinite(end));
                assertTrue(end >= previousEnd, "Fog must recede as rain fades");
                assertTrue((start == Float.MAX_VALUE && end == Float.MAX_VALUE) || start < end,
                        "Fog must have a positive span or be fully disabled");
                if (rain < 0.01F) {
                    assertTrue(start > distance, "The decaying rain tail must not fog visible terrain");
                }
                previousEnd = end;
            }
        }
    }
}
