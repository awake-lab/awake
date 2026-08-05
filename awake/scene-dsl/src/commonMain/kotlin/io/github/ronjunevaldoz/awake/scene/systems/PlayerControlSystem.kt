// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl
import io.github.ronjunevaldoz.awake.scene.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.components.OrbitControl
import io.github.ronjunevaldoz.awake.ui.context.UiInputOwnership

/**
 * Dedicated system for handling user input and mapping it to control intent components.
 * This is the ONLY system that should read/consume hardware snapshots.
 *
 * Decoupled from UI: queries a provided [UiInputOwnership] each frame to decide if
 * hardware events should be ignored.
 */
class PlayerControlSystem(
    private val rotateSpeed: Float = 0.01f,
    private val zoomSpeed: Float = 4f,
    private val pinchZoomSpeed: Float = 0.5f,
    /** Provider for the hardware snapshot for the current frame. */
    private val inputProvider: () -> InputSnapshot,
    /** Provider for the UI's input consumption results from the most recent UI pass. */
    private val uiResultProvider: () -> UiInputOwnership
) : System {
    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var wasDragging = false

    private var dragOriginX = 0f
    private var dragOriginY = 0f
    private var wasMovementDragging = false

    override fun update(world: World, delta: Float) {
        val input = inputProvider()
        val ui = uiResultProvider()
        
        val draggingPointer = input.pointerDown && !ui.isCaptured
        
        var yawDelta = 0f
        var pitchDelta = 0f

        // Handle Mouse Drag for rotation - blocked if UI captured the pointer
        if (draggingPointer) {
            if (wasDragging) {
                yawDelta = -(input.pointerX - lastPointerX) * rotateSpeed
                pitchDelta = -(input.pointerY - lastPointerY) * rotateSpeed
            }
            lastPointerX = input.pointerX
            lastPointerY = input.pointerY
            wasDragging = true
        } else {
            wasDragging = false
        }

        // --- Common Movement Intent (WASD) ---
        var rawMoveX = 0f
        var rawMoveZ = 0f
        if (input.keysDown.contains(Key.A) || input.keysDown.contains(Key.ArrowLeft)) rawMoveX -= 1f
        if (input.keysDown.contains(Key.D) || input.keysDown.contains(Key.ArrowRight)) rawMoveX += 1f
        if (input.keysDown.contains(Key.W) || input.keysDown.contains(Key.ArrowUp)) rawMoveZ -= 1f
        if (input.keysDown.contains(Key.S) || input.keysDown.contains(Key.ArrowDown)) rawMoveZ += 1f

        // Touch-drag movement (joystick style)
        if (rawMoveX == 0f && rawMoveZ == 0f && draggingPointer) {
            if (!wasMovementDragging) {
                dragOriginX = input.pointerX
                dragOriginY = input.pointerY
                wasMovementDragging = true
            } else {
                val dx = (input.pointerX - dragOriginX).coerceIn(-DRAG_RADIUS, DRAG_RADIUS) / DRAG_RADIUS
                val dy = (input.pointerY - dragOriginY).coerceIn(-DRAG_RADIUS, DRAG_RADIUS) / DRAG_RADIUS
                rawMoveX = dx
                rawMoveZ = dy
            }
        } else {
            wasMovementDragging = false
        }

        // --- Intent Mapping ---

        // 1. Orbit Zoom
        var distanceDelta = 0f
        val scrollDelta = if (!ui.isScrollConsumed) input.scrollDeltaY else 0f
        distanceDelta -= scrollDelta * pinchZoomSpeed

        // Apply intents to all entities that have OrbitControl
        world.queryEach(OrbitControl::class) { _, control ->
            control.yawDelta = yawDelta
            control.pitchDelta = pitchDelta
            control.distanceDelta = distanceDelta
        }

        // Apply intents to all entities that have FreeFlyControl
        world.queryEach(FreeFlyControl::class) { _, control ->
            control.yawDelta = yawDelta
            control.pitchDelta = pitchDelta
            control.moveX = rawMoveX
            control.moveY = 0f
            control.moveZ = -rawMoveZ + scrollDelta 
        }

        // Apply intents to all entities that have MovementControl (kinematic movement)
        world.queryEach(MovementControl::class) { _, control ->
            control.moveX = rawMoveX
            control.moveY = 0f
            control.moveZ = rawMoveZ
        }
    }

    private companion object {
        const val DRAG_RADIUS = 80f
    }
}
