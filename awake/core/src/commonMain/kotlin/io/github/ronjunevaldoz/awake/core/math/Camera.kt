// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.sqrt
import kotlin.math.tan

/**
 * A view + projection pair -- extracted out of `VulkanApplication`'s
 * `updateUniformBuffer` (previously computed inline, mixed in with the demo's own
 * cube-spin animation). Pure math, no GPU/backend dependency, so it lives in `awake-core`
 * rather than `awake-vulkan`: any future backend (WebGPU, Metal) needs the same view/
 * projection math, and testing it doesn't require a GPU.
 *
 * A `Camera` describes a **lens** -- eye, target, up, field of view, near/far -- and nothing
 * else. It deliberately stores no clip-space convention: the active
 * [io.github.ronjunevaldoz.awake.render.renderer.Renderer] owns that (it owns the API), and
 * passes its [ClipSpace] to [viewProjectionMatrix] at the moment the matrix is built. That is
 * why a scene, a demo or a test cannot bake in the wrong convention -- it never supplies one.
 */
class Camera(
    var eye: Vec3,
    var center: Vec3,
    var up: Vec3 = Vec3(0f, 1f, 0f),
    var fovYRadians: Float,
    var near: Float,
    var far: Float,
) {
    /** Which lens shape [viewProjectionMatrix] builds. Perspective unless a caller opts out,
     * so every camera built before this existed keeps the matrix it always had. */
    var projection: Projection = Projection.Perspective

    /** Half the vertical world extent an orthographic view covers; left/right follow from it
     * and the aspect ratio. Read only while [projection] is [Projection.Orthographic].
     *
     * Ortho has no perspective divide, so nothing else sets on-screen scale. A caller
     * toggling projection at runtime wants `distance * tan(fovYRadians / 2)`, which frames
     * exactly what the perspective lens frames at that distance. */
    var orthoHalfHeight: Float = DEFAULT_ORTHO_HALF_HEIGHT

    /** An enum plus a separate [orthoHalfHeight] rather than a sealed type carrying the
     * half-height: camera-driver systems reassign this every frame, and a variant with a
     * payload would allocate there (core-math skill Rule 2). */
    enum class Projection { Perspective, Orthographic }

    /** Returns `view * projection` in Mat4's own (Kotlin-operator) multiplication order,
     * which -- per [Mat4.times]'s convention (`A * B` computes the conventional `B * A`) --
     * is the conventional `projection * view`. A caller building a full MVP matrix combines
     * this with a model matrix the same way `VulkanApplication` already did:
     * `model * camera.viewProjectionMatrix(aspect, clipSpace)` (Kotlin order) gives the
     * conventional `projection * view * model`, the standard clip-space transform order.
     *
     * [clipSpace] has no default on purpose: it comes from the renderer that is about to
     * consume this matrix, and a default is exactly how a camera ends up silently baking in
     * some other backend's convention. */
    fun viewProjectionMatrix(aspect: Float, clipSpace: ClipSpace): Mat4 {
        val view = Mat4.setLookAt(eye = eye, center = center, up = up)
        val projectionMatrix = when (projection) {
            Projection.Perspective -> Mat4.perspective(
                fovY = fovYRadians,
                aspect = aspect,
                near = near,
                far = far,
                clipSpace = clipSpace,
            )

            // Same [clipSpace] through the same builder family, so ortho picks up the depth
            // convention (and the flipY below) exactly the way perspective does.
            Projection.Orthographic -> {
                val halfWidth = orthoHalfHeight * aspect
                Mat4.orthographic(
                    left = -halfWidth,
                    right = halfWidth,
                    bottom = -orthoHalfHeight,
                    top = orthoHalfHeight,
                    near = near,
                    far = far,
                    clipSpace = clipSpace,
                )
            }
        }
        if (clipSpace.flipY) {
            projectionMatrix.m11 *= -1f
        }
        return view * projectionMatrix
    }

    companion object {
        /**
         * The standard perspective lens, in the units people actually think in: field of view
         * in **degrees**, not radians.
         *
         * Exists because every call site was hand-writing `45f * (PI / 180.0).toFloat()` and
         * repeating the same near/far pair. Prefer this over the raw constructor.
         */
        fun perspective(
            eye: Vec3 = Vec3(0f, 0f, DEFAULT_EYE_DISTANCE),
            center: Vec3 = Vec3.ZERO,
            up: Vec3 = Vec3.UP,
            fovYDegrees: Float = DEFAULT_FOV_DEGREES,
            near: Float = DEFAULT_NEAR,
            far: Float = DEFAULT_FAR,
        ): Camera = Camera(
            eye = eye,
            center = center,
            up = up,
            fovYRadians = fovYDegrees.angleRad,
            near = near,
            far = far,
        )

        const val DEFAULT_FOV_DEGREES = 45f
        const val DEFAULT_NEAR = 0.1f
        const val DEFAULT_FAR = 100f

        /** What the default perspective lens frames at [DEFAULT_EYE_DISTANCE], so an untouched
         * camera switched to ortho keeps its subject roughly the same size. */
        val DEFAULT_ORTHO_HALF_HEIGHT = DEFAULT_EYE_DISTANCE * tan(DEFAULT_FOV_DEGREES.angleRad / 2f)

        private const val DEFAULT_EYE_DISTANCE = 5f
    }
}

/**
 * A world point in viewport pixels (origin top-left), or `null` when it sits behind the camera.
 *
 * Rejected by view-space depth rather than by the clip `w`: projecting a point behind the eye
 * anyway mirrors it onto the visible side, so a handle or label would appear -- and respond to
 * clicks -- somewhere it is not.
 */
fun Camera.projectToViewport(
    world: Vec3,
    viewProjection: Mat4,
    viewportWidth: Float,
    viewportHeight: Float,
): Vec2? {
    val clip = viewProjection.transformPosition(Vec4(world.x, world.y, world.z, 1f))
        .takeIf { isInFront(world) && it.w != 0f }
        ?: return null
    val ndcX = clip.x / clip.w
    val ndcY = clip.y / clip.w
    // NDC is +Y up; viewport pixels are +Y down.
    return Vec2(
        (ndcX * NDC_TO_UNIT + NDC_TO_UNIT) * viewportWidth,
        (NDC_TO_UNIT - ndcY * NDC_TO_UNIT) * viewportHeight,
    )
}

/** Depth along the view direction, against the near plane -- the check the clip `w` cannot make
 * (see [projectToViewport]). Written out so it allocates nothing on a per-frame path. */
private fun Camera.isInFront(world: Vec3): Boolean {
    val forwardX = center.x - eye.x
    val forwardY = center.y - eye.y
    val forwardZ = center.z - eye.z
    val forwardLength = sqrt(forwardX * forwardX + forwardY * forwardY + forwardZ * forwardZ)
    if (forwardLength < DEGENERATE_LENGTH) return false
    val depth = ((world.x - eye.x) * forwardX + (world.y - eye.y) * forwardY + (world.z - eye.z) * forwardZ) /
        forwardLength
    return depth > near
}

/**
 * The world-space ray through a viewport pixel -- what picking, drag-onto-a-plane and
 * click-to-place all start from. `null` when [viewProjection] cannot be inverted.
 *
 * Unprojects the near and far plane points of that pixel and joins them, rather than assuming
 * the ray starts at [eye]: that assumption is false for an orthographic camera, where every
 * ray is parallel and starts on the near plane.
 */
@Suppress("ReturnCount") // Each early exit is a distinct "this pixel has no ray" reason.
fun Camera.rayThroughViewport(
    viewportX: Float,
    viewportY: Float,
    viewProjection: Mat4,
    viewportWidth: Float,
    viewportHeight: Float,
): Ray? {
    if (viewportWidth <= 0f || viewportHeight <= 0f) return null
    val inverse = viewProjection.inverse() ?: return null

    val ndcX = (viewportX / viewportWidth) / NDC_TO_UNIT - 1f
    val ndcY = 1f - (viewportY / viewportHeight) / NDC_TO_UNIT

    val nearPoint = inverse.unprojectNdc(ndcX, ndcY, NEAR_NDC_DEPTH)
    val farPoint = inverse.unprojectNdc(ndcX, ndcY, FAR_NDC_DEPTH)
    if (nearPoint == null || farPoint == null) return null
    val dx = farPoint.x - nearPoint.x
    val dy = farPoint.y - nearPoint.y
    val dz = farPoint.z - nearPoint.z
    return if (dx * dx + dy * dy + dz * dz < DEGENERATE_LENGTH) null else Ray.through(nearPoint, farPoint)
}

/** One NDC point back through an already-inverted view-projection, with the perspective divide. */
private fun Mat4.unprojectNdc(ndcX: Float, ndcY: Float, ndcZ: Float): Vec3? {
    val unprojected = transformPosition(Vec4(ndcX, ndcY, ndcZ, 1f))
    if (unprojected.w == 0f) return null
    val inverseW = 1f / unprojected.w
    return Vec3(unprojected.x * inverseW, unprojected.y * inverseW, unprojected.z * inverseW)
}

/** Both backends' clip space runs depth 0..1 (see `Renderer.clipSpace`), so the near plane is 0
 * and the far plane is 1 -- not the -1..1 OpenGL convention. */
private const val NEAR_NDC_DEPTH = 0f
private const val FAR_NDC_DEPTH = 1f
private const val DEGENERATE_LENGTH = 1e-6f

/** NDC spans -1..1, viewport coordinates span 0..1 of their size. */
private const val NDC_TO_UNIT = 0.5f
