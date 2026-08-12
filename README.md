<p align="left">
  <img src="https://raw.githubusercontent.com/Jonyboylovespie/Lumenless/main/src/main/resources/assets/lumenless/icon.jpg" alt="Lumenless icon" width="200">
</p>

# Lumenless

Lumenless is a client-side Fabric mod that brings fullbright lighting, fog clearing, and targeted rendering optimizations together in one lightweight package. It is designed to make Minecraft clearer while improving performance rather than trading performance away.

Lumenless works with vanilla textures, biome tinting, entity effects, and normal rendering behavior. It does not require Iris or a shaderpack.

## Installation

1. Download and install Fabric Loader for your Minecraft version.
2. Download and install Fabric API.
3. Copy the Lumenless mod `.jar` into your Minecraft instance's `mods` folder.
4. Sodium is optional, but recommended for the best performance and compatibility.

Lumenless is client-only and should not be installed on a dedicated server.

## Settings

Open the Lumenless settings screen with `F7`, or use the configuration button in Mod Menu. Changes apply immediately and are saved to `config/lumenless.json`.

| Preset | Description |
| --- | --- |
| **Vanilla** | Disables Lumenless and restores vanilla light and ambient-occlusion behavior. |
| **Fullbright** | Provides fullbright visibility while retaining normal fog and block-light geometry behavior. |
| **Clarity** *(recommended)* | Enables every Lumenless feature: fullbright, fog clearing, and directional shading while preserving vanilla/Sodium ambient occlusion. |
| **Custom** | Configure individual Lumenless settings yourself. |

Cloud visibility remains controlled by Minecraft or Sodium.

## Performance

Lumenless applies its changes through the existing rendering paths instead of adding expensive fullscreen effects or extra rendering passes. Its optimizations can reduce unnecessary light sampling and fog work, while optional Sodium integrations use the same fast paths when Sodium is installed.

There are no additional framebuffers, deferred passes, shadows, bloom, SSAO, reflections, motion blur, anti-aliasing replacements, or Iris dependencies.

## Compatibility

The current build targets the Minecraft 26.2 and Fabric 26.2 APIs and requires Java 25. Sodium integrations are optional and are skipped automatically when Sodium is not installed. Because Minecraft's rendering internals can change between releases, revalidate Lumenless when upgrading Minecraft or Sodium.

## Support

Report bugs and request features on the [Lumenless issue tracker](https://github.com/Jonyboylovespie/Lumenless/issues).

## Development

Use Java 25 and the included Gradle wrapper to build the mod:

```text
./gradlew build
```

The compiled mod and sources jar are written to `build/libs/`. Sodium and Mod Menu are compile-only integrations and are not bundled with Lumenless.
