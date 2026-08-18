// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.inverse
import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape

/** `skybox.wgsl`'s uniform block, field for field: inverseViewProjection (mat4x4), cameraEye/
 * sunDirection/horizonColor/zenithColor/sunColor/moonColor (each a `vec4f`). Same "one number,
 * not N hand-copied literals" reason as [UniformLayout]. */
val SkyboxUniformLayout = UniformLayout(
    UniformField("inverseViewProjection", GpuDataShape.Mat4),
    UniformField("cameraEye", GpuDataShape.Vec4),
    UniformField("sunDirection", GpuDataShape.Vec4),
    UniformField("horizonColor", GpuDataShape.Vec4),
    UniformField("zenithColor", GpuDataShape.Vec4),
    UniformField("sunColor", GpuDataShape.Vec4),
    UniformField("moonColor", GpuDataShape.Vec4),
)

/** Warm sun, cooler and dimmer moon. Not [Renderer] fields: they are derived decoration, and
 * two more toggles would not buy a caller anything the horizon/zenith pair doesn't. */
@Suppress("MagicNumber") // Colour components.
val SUN_DISC_COLOR = floatArrayOf(1f, 0.92f, 0.72f, 1f)

@Suppress("MagicNumber") // Colour components.
val MOON_DISC_COLOR = floatArrayOf(0.72f, 0.78f, 0.88f, 1f)

/**
 * The float block both backends' skybox pipelines upload, assembled from data the 3D pass
 * already has in hand -- `null` when [viewProjection] is singular (no ray can be unprojected
 * from it), in which case the caller simply skips this frame's sky rather than uploading
 * garbage.
 *
 * [sunDirection] is the scene light's own direction (the direction it shines FROM), so the sun
 * disc lands where the light comes from and the moon lands opposite it -- no second light
 * source, and the sky moves whenever the light does.
 */
fun skyboxUniformFloats(
    viewProjection: Mat4,
    cameraEye: Vec3,
    sunDirection: Vec3,
    horizonColor: FloatArray,
    zenithColor: FloatArray,
): FloatArray? {
    val inverse = viewProjection.inverse() ?: return null
    return inverse.data +
        floatArrayOf(cameraEye.x, cameraEye.y, cameraEye.z, 0f) +
        floatArrayOf(sunDirection.x, sunDirection.y, sunDirection.z, 0f) +
        rgba(horizonColor) + rgba(zenithColor) + SUN_DISC_COLOR + MOON_DISC_COLOR
}

/** Pads a caller-supplied colour to the 4 floats the shader's `vec4f` reads -- a game is free
 * to hand [Renderer.horizonColor] a 3-float RGB. */
private fun rgba(color: FloatArray): FloatArray =
    floatArrayOf(color[0], color[1], color[2], color.getOrElse(ALPHA_INDEX) { 1f })

private const val ALPHA_INDEX = 3
