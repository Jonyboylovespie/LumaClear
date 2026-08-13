# Visual comparison pictures

This folder contains exactly 30 HUD-free Minecraft framebuffer captures: five fixed viewpoints × six configurations. Every PNG is 2048×1152 and uses Minecraft 26.2, MineBench Suite 1, and seed `7462910451`.

## Locations

| Prefix | Location | Dimension | Coordinate `(x, y, z)` | Angle `yaw, pitch` |
|---|---|---|---|---|
| `01` | Overworld panorama | Overworld | `(2048.5, 148.0, 2048.5)` | `-135.0°, 24.0°` |
| `02` | Underwater kelp forest | Overworld | `(2048.5, 52.0, 2048.5)` | `-35.0°, 6.0°` |
| `03` | Nether lava-cliff panorama | The Nether | `(1024.5, 112.0, 928.5)` | `0.0°, 8.0°` |
| `04` | Under-lava netherrack cavern | The Nether | `(1020.5, 29.0, 924.5)` | `25.0°, 0.0°` |
| `05` | Outer End islands | The End | `(2048.5, 144.0, -95.5)` | `0.0°, 10.0°` |

Seed for every location: `7462910451`.

## Filename format

`<location number>_<location>__<configuration>.png`

Configurations represented by the suffixes:

- `sodium-only`
- `iris-clarity`
- `lumenless-fullbright`
- `lumenless-default`
- `sodium-gamma-utils`
- `sodium-fullbright-ub`

The last configuration was verified with Polytone `26.2-6.3.1` and `Fullbright-UB-1.21 fub-6.0` loaded.
