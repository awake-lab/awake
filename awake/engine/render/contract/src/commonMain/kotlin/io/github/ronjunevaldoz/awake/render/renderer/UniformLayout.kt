// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset

/** One named field inside a shader's `Uniforms` struct, in the order it's concatenated --
 * [type] is the field's ACTUAL WGSL type (e.g. `material : vec4f`), not a hand-picked float
 * count: [floats] is derived from it, so a field can't be sized wrong for what the shader
 * struct actually declares. */
data class UniformField(val name: String, val type: GpuDataShape) {
    val floats: Int get() = type.uniformFloats
}

/** std140/WGSL-aligned float count for a uniform-buffer field -- [GpuDataShape.Vec3] pads to 4,
 * matching the alignment rule both backends' shaders already follow by hand today (e.g.
 * `lightDirection.w` carrying an extra scalar in its normally-unused pad slot). [GpuDataShape
 * .UInt4] has no meaningful value here -- not called for it (uniform fields are declared with
 * the other 5 cases only). See [io.github.ronjunevaldoz.awake.render.mesh.vertexByteSize] for
 * the unpadded vertex-buffer counterpart of the SAME [GpuDataShape]. */
val GpuDataShape.uniformFloats: Int
    get() = when (this) {
        GpuDataShape.Vec3 -> 4
        GpuDataShape.UInt4 -> error("$this has no uniform-buffer representation.")
        else -> componentCount
    }

/** Common fields every lit shader's `Uniforms` struct draws from -- a new shader composes
 * from these instead of hand-summing a new literal; a genuinely new field (not yet covered
 * here) is added once, to this list, not to N per-shader consts. */
object UniformFields {
    val Mvp = UniformField("mvp", GpuDataShape.Mat4)
    val LightDirection = UniformField("lightDirection", GpuDataShape.Vec4)
    val LightColor = UniformField("lightColor", GpuDataShape.Vec4)
    val LightMvp = UniformField("lightMvp", GpuDataShape.Mat4)
    val Model = UniformField("model", GpuDataShape.Mat4)
    val CameraPosition = UniformField("cameraPosition", GpuDataShape.Vec4)
    val FogColor = UniformField("fogColor", GpuDataShape.Vec4)
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

/** [Renderer.createMaterial] sized from [layout] instead of a bare `Int` -- the size a caller
 * actually has to get right (matching whatever pipeline/format this material will draw
 * through) is derived, not hand-typed. Prefer this over the raw `uniformFloatCount` parameter
 * whenever a [UniformLayout] for the target format already exists; both backends' `Material
 * .updateUniformBuffer` still assert the write fits regardless, so a wrong layout choice fails
 * loudly at the write site rather than as a native buffer-overrun. */
fun Renderer.createMaterial(
    layout: UniformLayout,
    texture: TextureAsset? = null,
    renderTarget: RenderTarget? = null,
    pbrTextures: PbrTextureSet? = null,
): Material = createMaterial(
    texture = texture,
    renderTarget = renderTarget,
    uniformFloatCount = layout.total,
    pbrTextures = pbrTextures,
)
