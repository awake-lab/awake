# Awake Asset glTF

Pure Kotlin glTF 2.0 binary (`.glb`) and JSON (`.gltf`) parser and scene loader for [Awake](../../README.md). Decodes meshes, materials, textures, vertex attributes, quantization extensions (`KHR_mesh_quantization`), and skeletal skinning hierarchies into engine types.

## Installation

```kotlin
implementation(project(":awake:asset:gltf"))
```

## Key Primitives

- `GltfParser` — parses raw glTF binary/JSON byte arrays into a structured `GltfDocument`.
- `GltfDocument` — parsed glTF container (nodes, meshes, materials, skins, animations).
- `GltfMesh` — extracted vertex buffers (positions, normals, UVs, joint indices/weights) and index buffers.
- `GltfSkinning` — skeletal joint hierarchy and inverse bind matrix decoding.
- `LoadedScene` — scene graph hierarchy ready for entity instantiation.

## Usage Example

```kotlin
import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser

val glbBytes: ByteArray = loadAssetBytes("character.glb")
val document = GltfParser.parse(glbBytes)

for (mesh in document.meshes) {
    println("Loaded mesh: ${mesh.name} with ${mesh.primitives.size} primitives")
}
```

## Related Modules

- [`:awake:asset:mesh-optimizer`](../mesh-optimizer/README.md) — mesh compression and LOD decimation tool.
- [`:awake:core:animation`](../../core/animation/README.md) — skeletal runtime consuming parsed skinning tracks.
- [`:awake:scene:authoring`](../../scene/authoring/README.md) — `scene { }` DSL instantiating glTF entities.
