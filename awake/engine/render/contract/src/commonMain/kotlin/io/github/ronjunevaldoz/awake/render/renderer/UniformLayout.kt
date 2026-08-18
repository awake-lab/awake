// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

/** One named block of floats inside a shader's `Uniforms` struct, in the order it's
 * concatenated. [floats] is the float count (`vec4f`-padded fields, e.g. `light`, are 8/4
 * even though only 3 components are read -- see `textured.wgsl`'s own doc comment on why). */
data class UniformField(val name: String, val floats: Int)

/** Common blocks every lit shader's `Uniforms` struct draws from -- a new shader composes
 * from these instead of hand-summing a new literal; a genuinely new block (not yet covered
 * here) is added once, to this list, not to N per-shader consts. */
object UniformFields {
    val Mvp = UniformField("mvp", 16)
    val Light = UniformField("light", 8)
    val LightMvp = UniformField("lightMvp", 16)
    val Model = UniformField("model", 16)
    val Camera = UniformField("camera", 4)
    val Fog = UniformField("fog", 4)
}

/** [fields], concatenated in order, is exactly the float array each shader's uniform buffer
 * must receive -- [total] is what `createMaterial(uniformFloatCount = ...)` and each
 * backend's own uniform-buffer size must agree on. [offsetOf] is for debugging/assertions,
 * not required to build the array (concatenation order already encodes it). */
class UniformLayout(vararg val fields: UniformField) {
    val total: Int = fields.sumOf { it.floats }
    fun offsetOf(field: UniformField): Int =
        fields.takeWhile { it !== field }.sumOf { it.floats }
}

/** `textured.wgsl` (glTF/PBR path): mvp, light, model, camera, glTF material factors, fog. */
val TexturedUniformLayout = UniformLayout(
    UniformFields.Mvp, UniformFields.Light, UniformFields.Model, UniformFields.Camera,
    UniformField("material", 12), UniformFields.Fog,
)

/** `lit_shadow.wgsl` (primary/lit + shadow path): mvp, light, lightMvp, model, camera,
 * metallic/roughness material factors, fog. */
val LitShadowUniformLayout = UniformLayout(
    UniformFields.Mvp, UniformFields.Light, UniformFields.LightMvp, UniformFields.Model,
    UniformFields.Camera, UniformField("material", 4), UniformFields.Fog,
)
