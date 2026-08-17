// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.times
import kotlin.math.abs

/** The fixed shadow-box constants both the real shadow pass (Vulkan's `RendererDraw3D
 * .lightViewProjection`) and the debug visualizer ([directionalShadowBox]) share -- one copy,
 * not two. ponytail: fixed shadow-box centered at the origin, not scene/camera-fit; upgrade
 * path is a per-frame bounding-box (or camera-frustum) fit once a demo's content moves far
 * from origin. */
const val SHADOW_LIGHT_DISTANCE = 15f
const val SHADOW_ORTHO_HALF_SIZE = 12f
const val SHADOW_NEAR = 0.1f
const val SHADOW_FAR = 40f

/** [view]/[projection] kept separate (not just their product) so a caller that needs the
 * combined matrix ([viewProjection], the real shadow pass) and a caller that needs the raw
 * [view] to invert (the debug visualizer's box wireframe) both work from one function. */
data class DirectionalShadowBox(val eye: Vec3, val view: Mat4, val projection: Mat4) {
    val viewProjection: Mat4 get() = view * projection
}

/** The directional light's own view-projection box, built the same "view * projection"
 * (Kotlin operator order) way [io.github.ronjunevaldoz.awake.core.math.Camera
 * .viewProjectionMatrix] builds a real camera's -- an orthographic projection instead of a
 * perspective one (correct for a directional/parallel-rays light). */
fun directionalShadowBox(
    direction: Vec3,
    clipSpace: ClipSpace,
    distance: Float = SHADOW_LIGHT_DISTANCE,
    halfSize: Float = SHADOW_ORTHO_HALF_SIZE,
    near: Float = SHADOW_NEAR,
    far: Float = SHADOW_FAR,
): DirectionalShadowBox {
    val normalizedDirection = direction.normalized()
    val eye = normalizedDirection * distance
    val up = if (abs(normalizedDirection.y) > 0.99f) Vec3(0f, 0f, 1f) else Vec3(0f, 1f, 0f)
    val view = Mat4.setLookAt(eye = eye, center = Vec3.ZERO, up = up)
    val projection = Mat4.orthographic(
        left = -halfSize,
        right = halfSize,
        bottom = -halfSize,
        top = halfSize,
        near = near,
        far = far,
        clipSpace = clipSpace,
    )
    if (clipSpace.flipY) projection.m11 *= -1f
    return DirectionalShadowBox(eye, view, projection)
}
