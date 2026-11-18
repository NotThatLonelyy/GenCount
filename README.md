# GenCount

A client-side mod that shows **how many iron, gold, diamond and emerald items are lying on the ground**,
as a colored count floating right above the drops.

| Drop | Color |
|---|---|
| Iron | Silver |
| Gold | Yellow |
| Diamond | Cyan |
| Emerald | Green |

- Drops that lie close together share one label, with **one colored line per type**. If diamonds and gold
  drop at the same spot, both counts are shown.
- Labels of different spots are pushed apart, so they never cover each other.
- Press **G** to toggle (change it under Controls).
- Client-side only. Nothing is needed on the server.

## Supported versions

| Minecraft | Loader | Java |
|---|---|---|
| 26.1.2, 26.2, 26.3 | Fabric | 25 |
| 1.8.9 | Forge | 8 |

On 1.8.9 there is no raw ore or iron nugget, so iron/gold ore blocks, ingots and gold nuggets are counted.
On 26.x raw ore, ingots and nuggets are counted.

## Building (all versions at once)

```
export JAVA_HOME=/path/to/jdk-25          # 26.x builds
export LEGACY_JAVA_HOME=/path/to/jdk-8    # 1.8.9 build (JDK 8 or 17)
./build-all.sh                            # Windows: build-all.bat
```

Every jar ends up in `artifacts/`. On GitHub, the `build` workflow does the same and uploads
`GenCount-all-versions`; pushing a `v*` tag also creates a release with all jars.

Layout:

```
core/            plain Java shared by every version (families, colors, grouping, overlap)
src/, versions/  Fabric code for 26.x (one folder per version holds only what differs)
legacy-1.8.9/    separate Forge build for 1.8.9, reuses core/
```

To add a new 26.x version, copy a folder in `versions/`, change its `gradle.properties`, and fix the
few version-specific lines.

## Credits

Inspired by [Drop Count](https://github.com/wlm3201/drop-count) by wlm3201.

## License

GPL-3.0, see `LICENSE`.
