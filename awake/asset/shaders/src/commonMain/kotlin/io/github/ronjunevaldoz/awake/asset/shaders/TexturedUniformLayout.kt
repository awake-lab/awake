// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.shaders

import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import io.github.ronjunevaldoz.awake.render.renderer.UniformField
import io.github.ronjunevaldoz.awake.render.renderer.UniformFields
import io.github.ronjunevaldoz.awake.render.renderer.UniformLayout

/** `textured.wgsl` (`resources/shaders/textured.wgsl`, this same module)'s `Uniforms` struct,
 * field for field: mvp, light direction/color, model, camera position, glTF material/
 * baseColorFactor/emissiveFactor (each their own `vec4f`, matching the shader source exactly --
 * not one hand-sized 12-float blob), fog. Lives here, not in `awake:engine:render:contract`,
 * because it describes ONE specific shader file this module owns -- the generic `UniformField`/
 * `UniformLayout` machinery it's built from stays in the render-contract module. */
val TexturedUniformLayout = UniformLayout(
    UniformFields.Mvp, UniformFields.LightDirection, UniformFields.LightColor,
    UniformFields.Model, UniformFields.CameraPosition,
    UniformField("material", GpuDataShape.Vec4),
    UniformField("baseColorFactor", GpuDataShape.Vec4),
    UniformField("emissiveFactor", GpuDataShape.Vec4),
    UniformFields.FogColor,
)
