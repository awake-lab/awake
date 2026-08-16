# Awake Core Geometry

Portable mesh geometry math for [Awake](../../README.md) — no file I/O, no asset-format
dependency, no platform-specific code. Compiles for every target this engine ships
(desktop/Android/iOS/wasmJs), so it's usable directly at runtime, not just from an offline
tool.

## Installation

```kotlin
implementation(project(":awake:core:geometry"))
```

## `MeshSimplifier` — mesh decimation, at runtime

Garland-Heckbert quadric-error-metric edge collapse: reduces a mesh's triangle count while
minimizing visual error, given only `positions`/`indices` (the same shape
`MeshGeometry`/`GltfMesh` already use).

```kotlin
import io.github.ronjunevaldoz.awake.core.geometry.MeshSimplifier

// positions: FloatArray (x,y,z per vertex), indices: IntArray (triangle list)
val result = MeshSimplifier.simplify(positions, indices, targetTriangleRatio = 0.5f)

result.positions   // FloatArray, fewer vertices
result.indices     // IntArray, ~half the original triangle count
result.vertexRemap // IntArray, one entry per ORIGINAL vertex -> its surviving index
```

Runtime use cases this unlocks:
- **Procedural/on-device LOD** — simplify a downloaded or procedurally generated mesh right
  before uploading it to the GPU, instead of requiring an artist to author separate LOD
  meshes ahead of time.
- **Adaptive quality** — pick `targetTriangleRatio` from the current device tier at load
  time (desktop vs. a lower-end Android device, say).

`result.vertexRemap` lets you downsample a parallel per-vertex array (normals, UVs, colors)
that `MeshSimplifier` itself doesn't touch (position-only in v1 — see the class doc comment):

```kotlin
val newNormals = FloatArray(result.positions.size)
for (originalVertex in normals.indices step 3) {
    val newVertex = result.vertexRemap[originalVertex / 3]
    newNormals[newVertex * 3] = normals[originalVertex]
    newNormals[newVertex * 3 + 1] = normals[originalVertex + 1]
    newNormals[newVertex * 3 + 2] = normals[originalVertex + 2]
}
```

For an offline, one-shot decimation pass over a `.gltf` file instead (batch LOD baking for a
content pipeline), see [awake:asset:mesh-optimizer](../../asset/mesh-optimizer/README.md),
which wraps this same class with file I/O.

## `NormalizedInt` — quantized vertex data

The integer-to-float normalization convention GPU vertex formats (and glTF's `normalized`
accessor flag) share: an integer packed into `[-1, 1]` (signed) or `[0, 1]` (unsigned) at
whatever bit width it was stored in.

```kotlin
import io.github.ronjunevaldoz.awake.core.geometry.NormalizedInt

NormalizedInt.signedByte(127)      // 1f
NormalizedInt.unsignedByte(255)    // 1f
NormalizedInt.signedShort(-32768)  // -1f (spec: max(c / 32767.0, -1.0))
NormalizedInt.unsignedShort(0)     // 0f
```

`awake-asset-gltf` already uses this to decode a quantized (`gltfpack`/meshoptimizer)
glTF export's `BYTE`/`SHORT` vertex accessors — most callers won't need to call this
directly unless decoding a different quantized format.

## Scope

Position-only mesh simplification in v1 — no UV-seam/attribute-discontinuity awareness (an
edge across a hard UV seam collapses the same as any other edge). No topology algorithms
beyond edge collapse (no retopology/remeshing — see the project's own mesh-optimization
planning notes for why that's explicitly out of scope). See `MeshSimplifier`'s own doc
comment for the exact upgrade paths flagged in the code.
