# LumaClear 1.0.1 — Minecraft 26.3 full laptop benchmark results

## Updated build and laptop methodology

- LumaClear 1.0.1+mc26.3, SHA-256 `1e19a36c5dda4c43b677da06334f9174060759165ed91bf03c326499bbb5fe4a`; includes AO-preserving light-sampling shortcuts and selective light-only render invalidation.
- Both earlier and updated binaries identify as 1.0.1; the SHA-256 identifies the exact measured build. See current built artifacts.
- Current publication metadata: campaign, actual dependencies, run order, and complete functional validation.
- Measured interval: 2026-11-12T22:36:29.565440353Z to 2026-11-13T01:04:31.851729351Z (UTC).
- GPU: Adreno X1-85; OpenGL: 4.6 (Core Profile) Mesa 26.2.6; native Wayland EGL.
- CPU: Qualcomm Oryon, 12 logical processors; ARM64 Java Eclipse Adoptium 25.0.4, 4 GiB heap.
- Actual framebuffer: 1840x1150; physical panel mode and desktop scaling are recorded separately in the campaign evidence.
- Render distance 12; simulation distance 8; native render scale; AO on; VSync off; unlimited FPS.
- Minecraft stores unlimited FPS as `maxFps=260`; its 26.3 frame limiter is invoked only below that value.
- MineBench Suite 1 standard: five iterations for each of five scenes, using separate copies of the original world and unchanged routes. Panorama/rebuild measure 30 seconds; flythroughs measure 60 seconds, with warmup and settling before measurements.
- Configuration order: lumaclear-fullbright, sodium, lumaclear-clarity, fullbright-ub, gamma-utils, iris-clarity. Controls are separate and are not substituted into primary results.
- External power, focus/fullscreen state, thermal readings and GPU/runtime evidence are archived for every configuration. All frame intervals, including long frames, are retained. FPS is not normalized or rescaled.
- The frame-pattern audit found no instance of the specific recurring presentation-stall signature from the withdrawn virtual-display campaign. This check does not rule out every presentation or driver issue.
- Laptop build, 27 unit tests and all four smoke configurations passed. See functional validation.
- Eight offline vertex-shader combinations passed on the laptop using its bundled ARM64 shaderc/glslang compiler, with zero warnings or errors. They cover Sodium 0.9.2 and 0.9.3-beta.1, normal/fullbright, and ordinary/alpha-only OIT. See shader sources, compiler details and results. Actual rendering and Iris/Fullbright UB activation preflights also ran on Adreno.

## Stability and interpretation

Sodium controls span 3.58% in median FPS. Sequential configuration differences can include session drift; small gains or regressions are not established by this matrix.

See all control measurements. The earlier quick laptop run had 7.86% baseline drift; it remains a separate diagnostic. These full-suite results measure the updated build, but do not isolate a code-only before/after improvement. Removing fog changes visible geometry, especially in the Nether.

## What the laptop measured

Fullbright's five-scene mean was 401.2 FPS versus Sodium's 410.8 FPS (-2.3%). Clarity measured 363.2 FPS excluding Nether versus Sodium's 359.6 FPS (+1.0%). These small observed differences do not establish a gain or regression under the recorded control variation.

Clarity's Nether scene measured 272.7 FPS versus Sodium's 615.7. Fog removal changes the visible workload; the separate Nether result and the all-scenes total are both retained. This campaign does not demonstrate a large overall speedup from the new lighting shortcuts.

## Earlier evidence

The desktop 4K publication and quick laptop comparison remain archived. The original virtual-display measurements remain withdrawn. Different machines/settings/artifact hashes make desktop-to-laptop FPS ratios unsuitable as evidence of a LumaClear code regression or speedup.


> **Performance comparisons require caution.** Sodium controls span 3.58% in median FPS. Sequential configuration differences can include session drift; small gains or regressions are not established by this matrix.

All six configurations completed Suite 1 standard mode with 25/25 valid iterations each and zero MineBench warnings (150/150 total).

Independent verification checked all 150 iterations and 2,881,650 archived frame intervals, including CSV integrity hashes, sample counts, measured durations, average FPS, 1% lows, and p99 frame times.

Laptop functional checks passed: build, 27 unit tests, four real mixin smoke configurations, and 8 offline shader compilation combinations. Full logs and compiler provenance are retained with the campaign.

The overall columns use the arithmetic mean of five scene-level medians. The four-scene columns exclude the Nether flythrough. Per-scene tables are the primary results; these summaries are not a universal performance score.

## Overall results

| Configuration | Average FPS | 1% low FPS | p99 frame time (ms) | Average FPS, excluding Nether | 1% low, excluding Nether | p99, excluding Nether (ms) | Valid iterations |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Sodium only | 410.8 | 180.6 | 4.99 | 359.6 | 168.7 | 5.31 | 25/25 |
| Iris + Clarity | 216.6 | 111.8 | 7.39 | 226.2 | 115.9 | 7.00 | 25/25 |
| LumaClear Fullbright | 401.2 | 176.0 | 5.03 | 351.7 | 169.3 | 5.35 | 25/25 |
| LumaClear Clarity | 345.1 | 164.6 | 5.26 | 363.2 | 173.6 | 5.06 | 25/25 |
| Sodium + Gamma Utils | 398.1 | 169.0 | 5.20 | 350.7 | 160.7 | 5.49 | 25/25 |
| Sodium + Fullbright UB | 426.1 | 180.8 | 4.86 | 378.2 | 174.2 | 5.15 | 25/25 |

## Average FPS by scene

| Scene | Sodium only | Iris + Clarity | LumaClear Fullbright | LumaClear Clarity | Sodium + Gamma Utils | Sodium + Fullbright UB |
| --- | --- | --- | --- | --- | --- | --- |
| Overworld panorama | 241.4 | 171.7 | 243.9 | 255.7 | 235.7 | 244.1 |
| Overworld flythrough | 306.3 | 196.7 | 306.9 | 309.1 | 287.8 | 303.7 |
| Nether flythrough | 615.7 | 178.2 | 599.2 | 272.7 | 587.7 | 617.8 |
| End flythrough | 641.1 | 346.3 | 613.1 | 639.1 | 661.9 | 718.8 |
| Chunk rebuild | 249.7 | 190.1 | 243.0 | 248.9 | 217.3 | 246.1 |

## 1% low FPS by scene

| Scene | Sodium only | Iris + Clarity | LumaClear Fullbright | LumaClear Clarity | Sodium + Gamma Utils | Sodium + Fullbright UB |
| --- | --- | --- | --- | --- | --- | --- |
| Overworld panorama | 156.3 | 109.6 | 150.3 | 167.1 | 157.0 | 153.9 |
| Overworld flythrough | 159.8 | 104.0 | 161.8 | 160.2 | 151.4 | 158.1 |
| Nether flythrough | 228.4 | 95.8 | 202.5 | 128.5 | 202.1 | 207.2 |
| End flythrough | 227.4 | 182.6 | 237.4 | 243.2 | 220.9 | 261.2 |
| Chunk rebuild | 131.2 | 67.2 | 127.8 | 124.0 | 113.5 | 123.7 |

## p99 frame time (ms) by scene

| Scene | Sodium only | Iris + Clarity | LumaClear Fullbright | LumaClear Clarity | Sodium + Gamma Utils | Sodium + Fullbright UB |
| --- | --- | --- | --- | --- | --- | --- |
| Overworld panorama | 6.22 | 8.17 | 6.12 | 5.29 | 6.02 | 6.11 |
| Overworld flythrough | 5.40 | 8.28 | 5.30 | 5.30 | 5.73 | 5.34 |
| Nether flythrough | 3.70 | 8.94 | 3.74 | 6.08 | 4.04 | 3.71 |
| End flythrough | 3.77 | 4.23 | 3.71 | 3.55 | 3.79 | 3.20 |
| Chunk rebuild | 5.84 | 7.34 | 6.28 | 6.09 | 6.41 | 5.95 |

## Difference from Sodium only

Positive FPS changes are better. Negative p99 frame-time changes are better.

| Configuration | Average FPS | 1% low | p99 time | Average FPS, excluding Nether | 1% low, excluding Nether | p99 time, excluding Nether |
| --- | --- | --- | --- | --- | --- | --- |
| Sodium only | +0.0% | +0.0% | +0.0% | +0.0% | +0.0% | +0.0% |
| Iris + Clarity | -47.3% | -38.1% | +48.2% | -37.1% | -31.3% | +32.0% |
| LumaClear Fullbright | -2.3% | -2.6% | +0.9% | -2.2% | +0.4% | +0.9% |
| LumaClear Clarity | -16.0% | -8.9% | +5.5% | +1.0% | +2.9% | -4.7% |
| Sodium + Gamma Utils | -3.1% | -6.5% | +4.3% | -2.5% | -4.7% | +3.4% |
| Sodium + Fullbright UB | +3.7% | +0.1% | -2.5% | +5.2% | +3.3% | -2.9% |

## Historical comparisons

Earlier desktop results use a different CPU/GPU, architecture, resolution, view distances, runtime and presentation path, and a pre-optimization LumaClear artifact. They remain archived separately. Cross-machine FPS differences do not measure the effect of the lighting changes; no desktop-to-laptop performance percentages are asserted.

## Recorded environment and versions

| Configuration | Versions | Shaderpack | Framebuffer | GPU | Java |
| --- | --- | --- | --- | --- | --- |
| Sodium only | minebench 1.0.1+mc26.3; fabricloader 0.20.1; fabric-api 0.166.0+26.3; sodium 0.9.2+mc26.3; cloth-config 26.3.159 | none (Iris not installed) | 1840x1150 | Adreno X1-85 | 25.0.4 |
| Iris + Clarity | minebench 1.0.1+mc26.3; fabricloader 0.20.1; fabric-api 0.166.0+26.3; sodium 0.9.2+mc26.3; iris 1.11.7+mc26.3; cloth-config 26.3.159 | Clarity 1.1.3.zip | 1840x1150 | Adreno X1-85 | 25.0.4 |
| LumaClear Fullbright | minebench 1.0.1+mc26.3; fabricloader 0.20.1; fabric-api 0.166.0+26.3; sodium 0.9.2+mc26.3; lumaclear 1.0.1+mc26.3; cloth-config 26.3.159 | none (Iris not installed) | 1840x1150 | Adreno X1-85 | 25.0.4 |
| LumaClear Clarity | minebench 1.0.1+mc26.3; fabricloader 0.20.1; fabric-api 0.166.0+26.3; sodium 0.9.2+mc26.3; lumaclear 1.0.1+mc26.3; cloth-config 26.3.159 | none (Iris not installed) | 1840x1150 | Adreno X1-85 | 25.0.4 |
| Sodium + Gamma Utils | minebench 1.0.1+mc26.3; fabricloader 0.20.1; fabric-api 0.166.0+26.3; sodium 0.9.2+mc26.3; gammautils 3.2.2; cloth-config 26.3.159 | none (Iris not installed) | 1840x1150 | Adreno X1-85 | 25.0.4 |
| Sodium + Fullbright UB | minebench 1.0.1+mc26.3; fabricloader 0.20.1; fabric-api 0.166.0+26.3; sodium 0.9.2+mc26.3; polytone 26.3-6.8.6; codecui 26.3-1.4.7; cloth-config 26.3.159 | none (Iris not installed) | 1840x1150 | Adreno X1-85 | 25.0.4 |

The machine-readable `summary.json` retains complete source reports, including environment fields, loaded mod versions, resource packs, iteration statistics, warnings, timestamps, and source hashes. Source reports, events, validation records, checksums and raw frame streams remain in the local, Git-ignored benchmark archive.

## Report provenance

| Configuration | Current run ID | Completed (UTC) | Report SHA-256 |
| --- | --- | --- | --- |
| Sodium only | ce5c82d1-661b-45a2-9d99-a53945c5738e | 2026-11-12T23:24:30.003670997Z | 51e4469509a255a1ed4658457201c611eb51f8247c5cf6988579866d7737bbd6 |
| Iris + Clarity | 6977ec53-5f97-4ab3-b3bb-edcf6f82b826 | 2026-11-13T01:04:31.851729351Z | 0fac7c463df5768ce915579475b8006050a466241fbf2a5e8ab6d856295c2742 |
| LumaClear Fullbright | d53ff0a8-c09d-43e9-9420-c61e20491982 | 2026-11-12T23:00:23.919588843Z | 4f10f114e2ea9d9e2850f8daa41cc37c40b08892fec974eaea9e4076cc65ce5c |
| LumaClear Clarity | 7e82b2ba-2bee-4a68-b928-58a9df41445c | 2026-11-12T23:48:37.584663100Z | 448151754c8d3c1062e6b4f24745959300786b05642782fd021242e05b5c63ae |
| Sodium + Gamma Utils | c4374410-daa5-4bdf-aa85-320cdf96043b | 2026-11-13T00:40:22.359160535Z | 18947c1d9cf257cf9b51d05eec262538f02ed04a965912d8fc44a0c94ea22469 |
| Sodium + Fullbright UB | 208b23f7-8270-4c92-af74-c64154a08445 | 2026-11-13T00:16:16.105989575Z | a3d5f5d197e2cf9d2c7de61b45678d2d54230f2ab8444d13ceaced0ee143fe48 |

## Local source archive

The complete source reports, configuration evidence, validation records, integrity hashes, controls and charts remain under `benchmarks/mc26.3-lumaclear1.0.1/`. The full laptop campaign is `laptop-full-20261112/`, and generated reports and figures are in `reports/`.

The entire `benchmarks/` and `tools/` folders are excluded from Git. This published Markdown report retains the results, environment, interpretation limits, run IDs and source-report hashes; the supporting files remain available locally.
