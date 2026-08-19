# Awake Asset Shaders

Shader uniform layouts, descriptor set layouts, and pipeline constant definitions for [Awake](../../README.md). Decouples shader data structures from platform-specific GPU backends.

## Installation

```kotlin
implementation(project(":awake:asset:shaders"))
```

## Key Primitives

- `LitShadowUniformLayout` — camera view/projection, directional light, cascade shadow matrices, and material parameters.
- `TexturedUniformLayout` — MVP transform matrix, tint color, and texture sampler bindings.

## Usage Example

```kotlin
import io.github.ronjunevaldoz.awake.asset.shaders.LitShadowUniformLayout

// Pack uniform buffer data for GPU upload
val uniformBytes = LitShadowUniformLayout.pack(
    viewProjection = camera.viewProjectionMatrix,
    lightDirection = light.direction,
    lightColor = light.color,
    ambientIntensity = 0.2f
)
```

## Related Modules

- [`:awake:engine:render:contract`](../../engine/render/contract/README.md) — GPU pipeline and descriptor set contracts.
- [`:awake:backend:vulkan`](../../backend/vulkan/README.md) — Vulkan descriptor set uploads.
- [`:awake:backend:webgpu`](../../backend/webgpu/README.md) — WebGPU bind group uploads.
