# Lumenless 1.0.0 — Performance Results

Author: Jonyboylovespie  
Test date: 2026-08-12  
Benchmark: MineBench Suite 1, standard mode, five iterations per scene  
World: `MineBench Suite 1` · seed `7462910451`

## Status

Four configurations completed the full benchmark successfully. Lumenless Default is **N/A** for this run: Minecraft crashed during the Nether render pass before MineBench could write a report. It is intentionally excluded from the numeric comparisons until it is rerun.

## Test environment

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.157.0+26.2
- Sodium 0.9.0+mc26.2
- Iris 1.11.1+mc26.2 for the shader configuration
- Clarity 1.1.3 for the shader configuration
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

The overall columns are the arithmetic mean of the five scene-level median values. They are a compact summary; the per-scene tables below are the primary results.

| Configuration | Average FPS | 1% low FPS | Median p99 frame time | Valid iterations |
|---|---:|---:|---:|---:|
| Sodium only | 1,826.3 | 673.2 | 1.53 ms | 25/25 |
| Iris + Clarity | 769.7 | 433.1 | 2.15 ms | 25/25 |
| Lumenless Vanilla | 1,825.2 | 675.9 | 1.52 ms | 25/25 |
| Lumenless Fullbright | 1,828.5 | 678.3 | 1.52 ms | 25/25 |
| Lumenless Default | N/A | N/A | N/A | N/A — crash before report |

## Average FPS by scene

| Scene | Sodium only | Iris + Clarity | Lumenless Vanilla | Lumenless Fullbright | Lumenless Default |
|---|---:|---:|---:|---:|---:|
| Overworld panorama | 809.8 | 611.7 | 807.7 | 809.9 | N/A |
| Overworld flythrough | 845.6 | 633.0 | 843.9 | 847.2 | N/A |
| Nether flythrough | 3,772.2 | 714.4 | 3,768.3 | 3,776.1 | N/A |
| End flythrough | 2,885.8 | 1,273.0 | 2,889.8 | 2,892.1 | N/A |
| Chunk rebuild | 818.0 | 616.5 | 816.4 | 817.3 | N/A |

## 1% low FPS by scene

| Scene | Sodium only | Iris + Clarity | Lumenless Vanilla | Lumenless Fullbright | Lumenless Default |
|---|---:|---:|---:|---:|---:|
| Overworld panorama | 492.2 | 412.8 | 488.5 | 495.7 | N/A |
| Overworld flythrough | 466.7 | 393.4 | 465.0 | 468.9 | N/A |
| Nether flythrough | 1,099.1 | 411.0 | 1,116.4 | 1,121.3 | N/A |
| End flythrough | 886.6 | 604.3 | 885.1 | 895.6 | N/A |
| Chunk rebuild | 421.5 | 344.1 | 424.8 | 410.2 | N/A |

## Difference from Sodium only

The percentages below compare the overall means in the summary table. A positive number is faster; a negative number is slower.

| Configuration | Average FPS change | 1% low change |
|---|---:|---:|
| Iris + Clarity | -57.9% | -35.7% |
| Lumenless Vanilla | -0.1% | +0.4% |
| Lumenless Fullbright | +0.1% | +0.8% |
| Lumenless Default | N/A | N/A |

## Interpretation

- Lumenless Vanilla and Lumenless Fullbright are effectively performance-neutral against Sodium on this workload; their differences are within normal run-to-run variation at these very high frame rates.
- Iris + Clarity is substantially more expensive, especially in the Nether and End flythroughs, which is expected for a shaderpack adding additional per-pixel rendering work.
- The Nether result is unusually high for Sodium and the Lumenless modes because this fixed route is relatively light on this machine. It should not be generalized to all Nether scenes.
- Lumenless Default requires a new run. The crash occurred after roughly ten minutes in the Nether flythrough and was a Sodium `RenderRegion.getResources() == null` render-region lifecycle error. No Default numbers are being inferred from the other Lumenless presets.