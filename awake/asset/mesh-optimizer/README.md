# Awake Mesh Optimizer

Offline CLI: simplifies a `.gltf` mesh's triangle count, for batch LOD-baking a content
pipeline wants pre-computed and shipped. I/O only — the actual algorithm lives in
[awake:core:geometry](../../core/geometry/README.md) (`MeshSimplifier`), which has zero
dependency on this module or on glTF at all and is directly usable at runtime instead — see
that module's README if you want to decimate a mesh on-device rather than as a build step.

## Usage

```bash
./gradlew :awake:asset:mesh-optimizer:decimate --args="input.gltf output.gltf 0.5"
```

- `input.gltf` — path to a `.gltf` file (embedded base64 buffers, same input shape
  `GltfParser.parse` reads — see `awake:asset:gltf`'s own `GltfParser.kt` doc comment).
- `output.gltf` — where to write the simplified mesh. Position-only in this pass (matching
  `MeshSimplifier`'s own v1 scope) — no normals/UVs/materials/skinning carry over.
- `targetRatio` — target triangle count as a fraction of the original (`0.5` = roughly half).
  Optional, defaults to `0.5`.

Run via `./gradlew :awake:asset:mesh-optimizer:run --args="..."` also works
(the `decimate` task additionally pins a stable `workingDir`, matching this repo's other
generator-style tool modules — see `awake:ui:tailwind-generator`).

## Not a general glTF exporter

`GltfWriter` (this module, not `awake-asset-gltf`) only writes a single mesh/primitive with a
`POSITION` accessor and indices — no materials, normals, UVs, or node hierarchy. It exists
to prove the decimate → write → re-import pipeline round-trips end to end (see
`MainSmokeTest`), not as a reusable glTF authoring tool.

## Scope

No Draco/meshopt binary compression, no UV-aware simplification, no retopology — see
`awake:core:geometry`'s own README for the full scope note.
