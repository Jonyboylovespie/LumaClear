# LumaClear

LumaClear is a client-side Fabric mod that brings fullbright lighting, fog clearing, and targeted rendering optimizations together in one lightweight package. It is designed to make Minecraft clearer while improving performance rather than trading performance away.

LumaClear works with vanilla textures, biome tinting, entity effects, and normal rendering behavior. It does not require Iris or a shaderpack.

<p align="left">
  <img src="https://raw.githubusercontent.com/Jonyboylovespie/LumaClear/refs/heads/main/ComparisonScreenshots/SideBySide/Underwater-compressed.jpg" alt="Underwater comparison: Vanilla, Fullbright, and LumaClear" width="100%">
</p>

## Installation

1. Download and install Fabric Loader for your Minecraft version.
2. Download and install Fabric API.
3. Copy the LumaClear mod `.jar` into your Minecraft instance's `mods` folder.
4. Sodium is optional, but recommended for the best performance and compatibility.

LumaClear is client-only and should not be installed on a dedicated server.

## Settings

Open the LumaClear settings screen with `F7`, or use the configuration button in Mod Menu. Changes apply immediately and are saved to `config/lumaclear.json`.

| Preset | Description |
| --- | --- |
| **Fullbright** | Provides fullbright visibility while retaining normal fog and block-light geometry behavior. |
| **Clarity** *(recommended)* | Enables every LumaClear feature: fullbright, fog clearing, and directional shading. |
| **Custom** | Configure individual LumaClear settings yourself. |
## Performance

LumaClear is built to provide a clearer view without paying the heavy rendering cost of a shaderpack. In the published MineBench results, the full LumaClear Default/Clarity configuration averaged 1,344.5 FPS, compared with 769.7 FPS for Iris + the Clarity shaderpack—a 74.7% higher average in the complete five-scene suite. Its 1% low result was also 32.9% higher. With the Nether flythrough excluded, where LumaClear intentionally renders much farther because Nether and distance fog are disabled, the advantage was 77.2% in average FPS.

LumaClear also slightly outperformed the tested generic fullbright alternatives while providing substantially more visual clarity. The fog-preserving LumaClear Fullbright preset measured 0.9% higher average FPS and 2.6% higher 1% lows than the Fullbright UB resource pack, and its 1% lows were 1.3% higher than Sodium + Gamma Utils. These differences are small and should be treated as near-parity rather than a universal guarantee, but they show that the extra clarity features do not require a performance penalty in the normal-fog preset. The full Default/Clarity preset was 3.6% ahead of Sodium after excluding the Nether route, and 3.7–4.0% ahead of the two generic fullbright comparisons on that same basis.

The main tradeoff is intentional: removing distance and Nether fog exposes more distant terrain, so the Default/Clarity preset can render more geometry than fog-preserving alternatives. In the benchmark’s long Nether flythrough, that reduced Default’s result to 1,167.8 FPS versus roughly 3,700 FPS for fog-preserving configurations. This is the cost of seeing farther, not a sign that the mod’s core lighting optimizations are ineffective. The published results use a fixed route and one test system, so actual gains will vary by hardware, view distance, scene complexity, and configuration.

LumaClear applies its changes through existing rendering paths instead of adding expensive fullscreen effects or extra rendering passes. Its optimizations can reduce unnecessary light sampling and fog work, while optional Sodium integrations use the same fast paths when Sodium is installed. See the [published performance results](https://github.com/Jonyboylovespie/LumaClear/blob/main/PUBLISHED_PERFORMANCE_RESULTS.md) for the complete tables and methodology.

There are no additional framebuffers, deferred passes, shadows, bloom, SSAO, reflections, motion blur, anti-aliasing replacements, or Iris dependencies.

## Compatibility

The current build targets the Minecraft 26.2 and Fabric 26.2 APIs and requires Java 25. Sodium integrations are optional and are skipped automatically when Sodium is not installed. Because Minecraft's rendering internals can change between releases, revalidate LumaClear when upgrading Minecraft or Sodium.

## Support

Report bugs and request features on the [LumaClear issue tracker](https://github.com/Jonyboylovespie/LumaClear/issues).

## Development

Use Java 25 and the included Gradle wrapper to build the mod:

```text
./gradlew build
```

The compiled mod and sources jar are written to `build/libs/`. Sodium and Mod Menu are compile-only integrations and are not bundled with LumaClear.
