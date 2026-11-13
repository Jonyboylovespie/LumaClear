# LumaClear

LumaClear is a client-side Fabric mod that brings fullbright lighting, fog clearing, and targeted rendering optimizations together in one lightweight package. It is designed to make Minecraft clearer while improving performance comparatively to other mods that offer clarity to this extent.

LumaClear works with vanilla textures, biome tinting, entity effects, and normal rendering behavior. It does not require Iris or a shaderpack.

<p align="left">
  <img src="https://raw.githubusercontent.com/Jonyboylovespie/LumaClear/refs/heads/main/ComparisonScreenshots/SideBySide/Underwater-compressed.jpg" alt="Underwater comparison: Vanilla, Fullbright, and LumaClear" width="100%">
</p>

## Installation

1. Download and install Fabric Loader for your Minecraft version.
2. Download and install Fabric API 0.166.0 or newer for Minecraft 26.3.
3. Copy the LumaClear mod `.jar` into your Minecraft instance's `mods` folder.
4. Sodium is optional, but recommended for the best performance and compatibility.

LumaClear is client-only and should not be installed on a dedicated server.

## Settings

Open the LumaClear settings screen with `F7`, or use the configuration button in Mod Menu. Changes apply immediately and are saved to `config/lumaclear.json`.

The preset button supports mouse clicks and keyboard activation with Enter or Space; hold Shift to cycle backwards. Directional shading can be disabled independently in Custom while retaining ambient occlusion. Fog controls preserve other enabled fog types, including status effects.

Fullbright skips terrain block/sky-light sampling while preserving ambient occlusion, and ignores render rebuilds caused solely by lighting updates. Block changes still rebuild normally. Switching fullbright on or off rebuilds terrain to restore the correct baked lighting. These shortcuts fall back to normal sampling when Iris is installed.

| Preset | Description |
| --- | --- |
| **Fullbright** | Applies fullbright terrain lighting while retaining normal fog, ambient occlusion, and directional shading. |
| **Clarity** *(recommended)* | Enables every LumaClear feature: fullbright, fog clearing, and directional shading. |
| **Custom** | Configure individual LumaClear settings yourself. |

## Performance

The full Minecraft 26.3 laptop suite for the updated LumaClear 1.0.1 build completed **150/150 valid iterations with zero MineBench warnings**. It covers Sodium, Iris + Clarity, both LumaClear presets, Gamma Utils at 1500%, and Fullbright UB 7.0 with Polytone.

On the Adreno X1-85 laptop at 1840x1150, view distance 12 and simulation distance 8, Fullbright measured **401.2 FPS** and Clarity **345.1 FPS** across the five scenes. These are means of scene medians, not universal FPS expectations. Ambient occlusion remains enabled.

Sodium controls span 3.58% in median FPS. Sequential configuration differences can include session drift; small gains or regressions are not established by this matrix.

Every raw interval was independently verified and retained locally. Benchmark source data, charts, and helper tools remain in the ignored `benchmarks/` and `tools/` folders. Previous desktop measurements use different hardware/settings and remain historical evidence. See the [complete laptop results, controls, artifact hash and methodology](PUBLISHED_PERFORMANCE_RESULTS.md).

## Compatibility

The current build targets the Minecraft 26.3 and Fabric 26.3 APIs and requires Java 25. Sodium integrations are optional and are skipped automatically when Sodium is not installed. Because Minecraft's rendering internals can change between releases, revalidate LumaClear when upgrading Minecraft or Sodium.

## Support

Report bugs and request features on the [LumaClear issue tracker](https://github.com/Jonyboylovespie/LumaClear/issues).

## Development

Use Java 25 and the included Gradle wrapper to build the mod:

```text
./gradlew build
```

The compiled mod and sources jar are written to `build/libs/`. Sodium and Mod Menu are compile-only integrations and are not bundled with LumaClear. The build includes unit tests and the headless smoke check without Sodium.

Run the real Fabric mixin and CPU behavior checks without opening a game window:

```text
./gradlew mixinSmokeTest
./gradlew mixinSmokeTest -PsmokeSodiumVersion=0.9.2+mc26.3
./gradlew mixinSmokeTest -PsmokeSodiumVersion=0.9.3-beta.1+mc26.3
./gradlew mixinSmokeTest -PsmokeSodiumVersion=0.9.3-beta.4+mc26.3
```

The smoke-test fixture stays in `build/smoke/`, outside the distributable jars.
