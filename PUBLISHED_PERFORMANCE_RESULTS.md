# LumaClear 1.0.0 — Performance Results

Author: Jonyboylovespie  
Test date: 2026-08-12  
Benchmark: MineBench Suite 1, standard mode, five iterations per scene  
World: `MineBench Suite 1` · seed `7462910451`

## Status

Six configurations completed the full benchmark successfully. Every configuration produced 25/25 valid iterations with zero MineBench warnings.

## Configuration links

- Iris + [Clarity 1.1.3](https://modrinth.com/shader/clarityshader/version/1.1.3)
- Sodium + [Gamma Utils](https://modrinth.com/mod/gamma-utils) 3.1.1+Fabric, gamma set to 1500%
- Sodium + [Fullbright UB](https://modrinth.com/resourcepack/fullbright-ub) 6.0, with Polytone 26.2-6.3.1

## Test environment

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.157.0+26.2
- Sodium 0.9.0+mc26.2
- Iris 1.11.1+mc26.2 for the shader configuration
- CPU: AMD Ryzen 7 9800X3D, 16 logical processors
- GPU: AMD Radeon RX 9070 XT
- Framebuffer: 3168×1782
- Render distance: 32 chunks
- Simulation distance: 32 chunks
- VSync: off · FPS limit: 260
- MineBench workload target: balanced/native
- All reported iterations were valid and all completed reports contained zero MineBench warnings.

These results describe this hardware, driver, game build, resource-pack set, and graphics configuration. They are not universal performance guarantees. FPS was not the only measured value; the raw MineBench reports also contain frame-time percentiles, memory, GC, validity, and environment data.

## Overall results

The normal columns are the arithmetic mean of the five scene-level median values. The last three columns repeat that summary after excluding the Nether flythrough. They are a compact summary; the per-scene tables below are the primary results.

| Configuration | Average FPS | 1% low FPS | Median p99 frame time | Valid iterations | Average FPS, excluding Nether | 1% low FPS, excluding Nether | Median p99 frame time, excluding Nether |
|---|---:|---:|---:|---:|---:|---:|---:|
| Sodium only | 1,826.3 | 673.2 | 1.53 ms | 25/25 | 1,339.8 | 566.8 | 1.71 ms |
| Iris + Clarity | 769.7 | 433.1 | 2.15 ms | 25/25 | 783.5 | 438.6 | 2.13 ms |
| LumaClear Fullbright | 1,828.5 | 678.3 | 1.52 ms | 25/25 | 1,341.6 | 567.6 | 1.70 ms |
| LumaClear Default | 1,344.5 | 575.4 | 1.66 ms | 25/25 | 1,388.7 | 582.2 | 1.66 ms |
| Sodium + Gamma Utils | 1,829.5 | 669.4 | 1.52 ms | 25/25 | 1,339.8 | 561.1 | 1.70 ms |
| Sodium + Fullbright UB | 1,812.0 | 661.3 | 1.54 ms | 25/25 | 1,335.0 | 562.1 | 1.73 ms |

## Average FPS by scene

| Scene | Sodium only | Iris + Clarity | LumaClear Fullbright | LumaClear Default | Sodium + Gamma Utils | Sodium + Fullbright UB |
|---|---:|---:|---:|---:|---:|---:|
| Overworld panorama | 809.8 | 611.7 | 809.9 | 842.4 | 807.0 | 804.5 |
| Overworld flythrough | 845.6 | 633.0 | 847.2 | 882.1 | 842.3 | 841.8 |
| Nether flythrough | 3,772.2 | 714.4 | 3,776.1 | 1,167.8 | 3,788.4 | 3,720.2 |
| End flythrough | 2,885.8 | 1,273.0 | 2,892.1 | 2,977.3 | 2,892.6 | 2,878.4 |
| Chunk rebuild | 818.0 | 616.5 | 817.3 | 853.0 | 817.2 | 815.3 |

## 1% low FPS by scene

| Scene | Sodium only | Iris + Clarity | LumaClear Fullbright | LumaClear Default | Sodium + Gamma Utils | Sodium + Fullbright UB |
|---|---:|---:|---:|---:|---:|---:|
| Overworld panorama | 492.2 | 412.8 | 495.7 | 503.9 | 497.2 | 486.2 |
| Overworld flythrough | 466.7 | 393.4 | 468.9 | 479.9 | 461.4 | 464.4 |
| Nether flythrough | 1,099.1 | 411.0 | 1,121.3 | 548.3 | 1,102.7 | 1,058.1 |
| End flythrough | 886.6 | 604.3 | 895.6 | 901.3 | 878.4 | 875.4 |
| Chunk rebuild | 421.5 | 344.1 | 410.2 | 443.7 | 407.2 | 422.6 |

## Difference from Sodium only

The first two percentage columns compare all five scene-level medians against Sodium only. The last two repeat the comparison after excluding the Nether flythrough. A positive number is faster; a negative number is slower.

| Configuration | Average FPS change | 1% low change | Average FPS change, excluding Nether | 1% low change, excluding Nether |
|---|---:|---:|---:|---:|
| Iris + Clarity | -57.9% | -35.7% | -41.5% | -22.6% |
| LumaClear Fullbright | +0.1% | +0.8% | +0.1% | +0.1% |
| LumaClear Default | -26.4% | -14.5% | +3.6% | +2.7% |
| Sodium + Gamma Utils | +0.2% | -0.6% | -0.0% | -1.0% |
| Sodium + Fullbright UB | -0.8% | -1.8% | -0.4% | -0.8% |

## Interpretation

- LumaClear Default averages 26.4% below Sodium across all five scenes and is 14.5% lower on the 1% low metric. That headline is dominated by the Nether flythrough: Default averages 1,167.8 FPS there, compared with 3,772.2 FPS for Sodium only.
- The Nether result is expected from what Default enables. It removes Nether fog and distance fog, so the renderer no longer receives the visibility culling that vanilla fog provides. Much more of the distant Nether terrain remains visible and must be processed, especially in this benchmark’s long flythrough. The result is a visibility-versus-throughput tradeoff, not evidence that every LumaClear feature is intrinsically slower.
- The Nether comparison is also especially stark because Sodium-only and LumaClear Fullbright retain normal fog and therefore benefit from the same distant-scene cutoff. Their roughly 3,700 FPS Nether scores are not directly comparable to Default’s fog-free view as an equal-visibility workload.
- Excluding the Nether flythrough, LumaClear Default averages 3.6% faster than Sodium and is 2.7% faster on the 1% low metric. This better represents Default’s performance in the Overworld panorama, Overworld flythrough, End flythrough, and chunk-rebuild scenes, where its fullbright, fog, and directional-rendering changes do not create the same enormous distant-Nether workload.
- LumaClear Fullbright is effectively performance-neutral against Sodium on this workload because it changes visibility lighting while retaining normal fog behavior. Its differences are within normal run-to-run variation at these very high frame rates.
- Sodium + Gamma Utils is effectively performance-neutral here: its +0.2% average-FPS change is within normal run-to-run variation, while its high-gamma behavior does not add a meaningful rendering cost.
- Sodium + Fullbright UB is also close to baseline, at -0.8% average FPS and -1.8% on the 1% low metric. The small regression is not evidence of a major performance cost on this workload.
- Iris + Clarity is substantially more expensive, especially in the Nether and End flythroughs, which is expected for a shaderpack adding additional per-pixel rendering work.
- These are fixed-route results. The Nether route is unusually light for fog-preserving configurations on this machine, while Default intentionally exposes more of it; neither result should be generalized to every Nether scene or camera distance.
