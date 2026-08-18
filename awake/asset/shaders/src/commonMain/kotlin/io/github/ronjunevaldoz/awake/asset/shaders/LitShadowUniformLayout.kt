// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.shaders

import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import io.github.ronjunevaldoz.awake.render.renderer.UniformField
import io.github.ronjunevaldoz.awake.render.renderer.UniformFields
import io.github.ronjunevaldoz.awake.render.renderer.UniformLayout

/** `lit_shadow.wgsl`'s `Uniforms` struct, field for field: mvp, light direction/color,
 * lightMvp, model, camera position, metallic/roughness `material` (packed into one `vec4f`,
 * matching the shader source), fog. `lit_shadow.wgsl` itself is NOT yet deduplicated into this
 * module's shared `resources/shaders/` directory (still a per-sample copy in each of
 * `samples/scene3d-playground`/`samples/studio`, kept in sync by hand) -- this layout still
 * lives here rather than in the generic render-contract module because it describes authored
 * shader content, the same reasoning `TexturedUniformLayout` documents. */
val LitShadowUniformLayout = UniformLayout(
    UniformFields.Mvp, UniformFields.LightDirection, UniformFields.LightColor,
    UniformFields.LightMvp, UniformFields.Model, UniformFields.CameraPosition,
    UniformField("material", GpuDataShape.Vec4),
    UniformFields.FogColor,
)
