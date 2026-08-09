// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls.systems

import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.ui.context.UiInputOwnership
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Drives the [ActiveCamera]'s pose from its [CameraComponent].
 *
 * Every mode derives its aim from the single [forwardFrom] basis, so `yaw`/`pitch` mean the
 * same thing in all of them: dragging right always looks right, dragging down always looks
 * down. (Modes previously disagreed -- one treated the angles as a view direction and another
 * as the eye's position on a sphere around the target, which are negatives of each other, so
 * switching modes mirrored the controls.)
 */
class CameraSystem(
    private val inputProvider: () -> InputSnapshot,
    private val uiResultProvider: () -> UiInputOwnership,
) : System {
    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var wasDragging = false

    // Scratch vectors -- this runs every frame, so the pose math must not allocate.
    private val forward = Vec3()
    private val desiredEye = Vec3()

    override fun update(world: World, delta: Float) {
        val input = inputProvider()
        val ui = uiResultProvider()

        // `isCaptured`, not `blocksGameplayKeys`: look/zoom here are pointer gestures, and a
        // focused text field owns the keyboard, not the mouse -- dragging the world while a
        // field has focus is legitimate.
        val dragging = input.pointerDown && !ui.isCaptured
        val dx = if (dragging && wasDragging) input.pointerX - lastPointerX else 0f
        val dy = if (dragging && wasDragging) input.pointerY - lastPointerY else 0f
        lastPointerX = input.pointerX
        lastPointerY = input.pointerY
        wasDragging = dragging

        world.queryEach(Camera::class, CameraComponent::class) { entity, camera, config ->
            if (!world.has(entity, ActiveCamera::class)) return@queryEach

            val targetTransform = config.targetEntity?.let { world.get<Transform>(it) }

            if (config.needsReset) {
                resetCameraForMode(config, camera, targetTransform)
                config.needsReset = false
            }

            // Only accumulate an axis the current mode actually reads -- see CameraMode.
            if (config.mode.usesYaw) {
                // `+`, not `-`: with yaw 0 facing -Z and +Y up, screen-right is +X, and
                // forward.x is sin(yaw). Subtracting here turned the view left when the
                // pointer moved right.
                config.yaw += dx * LOOK_SENSITIVITY
            }
            if (config.mode.usesPitch) {
                config.pitch = (config.pitch - dy * LOOK_SENSITIVITY)
                    .coerceIn(-PITCH_LIMIT, PITCH_LIMIT)
            }
            if (config.mode.usesZoom) {
                val scroll = if (!ui.isScrollConsumed) input.scrollDeltaY else 0f
                config.distance = (config.distance - scroll * ZOOM_SENSITIVITY)
                    .coerceIn(config.minDistance, config.maxDistance)
            }

            updateCameraPose(config, camera, targetTransform, delta)
        }
    }

    private fun resetCameraForMode(config: CameraComponent, camera: Camera, target: Transform?) {
        when (config.mode) {
            CameraMode.FirstPerson -> {
                config.pitch = 0f
                config.yaw = 0f
                config.distance = 0f
            }

            CameraMode.ThirdPerson -> {
                config.pitch = THIRD_PERSON_PITCH
                config.yaw = 0f
                config.distance = THIRD_PERSON_DISTANCE
            }

            CameraMode.Cinematic -> {
                // Park the eye once; from here on only the aim tracks the target.
                if (target != null) {
                    camera.camera.eye.set(
                        target.position.x + CINEMATIC_OFFSET,
                        target.position.y + CINEMATIC_HEIGHT,
                        target.position.z + CINEMATIC_OFFSET,
                    )
                }
            }

            CameraMode.TopDown -> {
                config.distance = TOP_DOWN_DISTANCE
            }
        }
    }

    private fun updateCameraPose(
        config: CameraComponent,
        camera: Camera,
        target: Transform?,
        dt: Float,
    ) {
        // Every mode aims at a target; without one there is no pose to compute.
        if (target == null) return
        val core = camera.camera
        when (config.mode) {
            CameraMode.FirstPerson -> {
                core.eye.set(target.position).add(config.offsetPosition)
                forwardFrom(config.yaw, config.pitch, forward)
                core.center.set(core.eye).add(forward)
            }

            CameraMode.ThirdPerson -> {
                // Look at the target's eye level, and sit `distance` back along the same
                // forward the first-person mode would use.
                core.center.set(target.position).add(config.offsetPosition)
                forwardFrom(config.yaw, config.pitch, forward)
                desiredEye.set(core.center).sub(forward.scale(config.distance))
                // Exponential smoothing so a mode switch eases in instead of cutting.
                core.eye.lerp(desiredEye, (1f - exp(-SMOOTHING * dt)).coerceIn(0f, 1f))
            }

            CameraMode.Cinematic -> {
                core.center.set(target.position)
            }

            CameraMode.TopDown -> {
                core.center.set(target.position)
                forwardFrom(config.yaw, TOP_DOWN_PITCH, forward)
                core.eye.set(core.center).sub(forward.scale(config.distance))
            }
        }
    }

    /**
     * The shared aim basis: +yaw turns right, +pitch looks up, and yaw 0 faces -Z. Writes into
     * [out] rather than returning a new vector so per-frame callers allocate nothing.
     */
    private fun forwardFrom(yaw: Float, pitch: Float, out: Vec3) {
        val cp = cos(pitch)
        out.set(sin(yaw) * cp, sin(pitch), -cos(yaw) * cp)
    }

    private companion object {
        const val LOOK_SENSITIVITY = 0.005f
        const val ZOOM_SENSITIVITY = 0.5f

        /** Kept short of a right angle: at exactly +/-90 degrees the aim is parallel to the
         * camera's up vector and `setLookAt`'s cross product collapses. */
        const val PITCH_LIMIT = (85.0 * PI / 180.0).toFloat()
        const val TOP_DOWN_PITCH = (-60.0 * PI / 180.0).toFloat()

        const val THIRD_PERSON_PITCH = -0.4f
        const val THIRD_PERSON_DISTANCE = 5f
        const val TOP_DOWN_DISTANCE = 15f
        const val CINEMATIC_OFFSET = 10f
        const val CINEMATIC_HEIGHT = 5f

        const val SMOOTHING = 10f
    }
}
