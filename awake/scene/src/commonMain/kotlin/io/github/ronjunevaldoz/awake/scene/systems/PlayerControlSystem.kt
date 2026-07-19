// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl
import io.github.ronjunevaldoz.awake.scene.components.OrbitControl
import io.github.ronjunevaldoz.awake.ui.UiInputResult

/**
 * Dedicated system for handling user input and mapping it to control intent components.
 * This is the ONLY system that should read/consume raw hardware [Input] deltas.
 *
 * Decoupled from UI: queries a provided [UiInputResult] each frame to decide if
 * hardware events should be ignored.
 */
class PlayerControlSystem(
    private val rotateSpeed: Float = 0.01f,
    private val zoomSpeed: Float = 4f,
    private val pinchZoomSpeed: Float = 0.5f,
    /** Provider for the UI's input consumption results from the most recent UI pass. */
    private val uiResultProvider: () -> UiInputResult
) : System {
    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var wasDragging = false

    override fun update(world: World, delta: Float) {
        val ui = uiResultProvider()
        val draggingPointer = Input.pointerDown && !ui.isCaptured
        
        var yawDelta = 0f
        var pitchDelta = 0f
        var distanceDelta = 0f

        // Handle Mouse Drag for rotation - blocked if UI captured the pointer
        if (draggingPointer) {
            if (wasDragging) {
                yawDelta = -(Input.pointerX - lastPointerX) * rotateSpeed
                pitchDelta = -(Input.pointerY - lastPointerY) * rotateSpeed
            }
            lastPointerX = Input.pointerX
            lastPointerY = Input.pointerY
            wasDragging = true
        } else {
            wasDragging = false
        }

        // Handle Keyboard for Zoom - keyboard is never blocked by UI hovering/capture
        if (Input.isKeyDown(Key.W)) distanceDelta -= zoomSpeed * delta
        if (Input.isKeyDown(Key.S)) distanceDelta += zoomSpeed * delta

        // Handle Scroll for Zoom - blocked if a UI widget already used the delta
        val scrollDelta = if (!ui.isScrollConsumed) Input.scrollDeltaY else 0f
        
        val orbitDistanceDelta = distanceDelta - scrollDelta * pinchZoomSpeed

        // Apply intents to all entities that have OrbitControl
        world.queryEach(OrbitControl::class) { _, control ->
            control.yawDelta = yawDelta
            control.pitchDelta = pitchDelta
            control.distanceDelta = orbitDistanceDelta
        }

        // Apply intents to all entities that have FreeFlyControl
        world.queryEach(FreeFlyControl::class) { _, control ->
            control.yawDelta = yawDelta
            control.pitchDelta = pitchDelta
            
            // FreeFly movement logic (WASD + Scroll as dolly)
            var rawMoveX = 0f
            val rawMoveY = 0f
            var rawMoveZ = 0f
            
            if (Input.isKeyDown(Key.W) || Input.isKeyDown(Key.ArrowUp)) rawMoveZ += 1f
            if (Input.isKeyDown(Key.S) || Input.isKeyDown(Key.ArrowDown)) rawMoveZ -= 1f
            if (Input.isKeyDown(Key.D) || Input.isKeyDown(Key.ArrowRight)) rawMoveX += 1f
            if (Input.isKeyDown(Key.A) || Input.isKeyDown(Key.ArrowLeft)) rawMoveX -= 1f
            
            // Scroll deltas move forward/backward (dolly) in FreeFly
            rawMoveZ += scrollDelta
            
            control.moveX = rawMoveX
            control.moveY = rawMoveY
            control.moveZ = rawMoveZ
        }
    }
}
