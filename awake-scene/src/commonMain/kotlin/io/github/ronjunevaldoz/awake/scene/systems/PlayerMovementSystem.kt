/*
 * Awake
 * Awake.awake-scene.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * MVP1a (see docs/MMORPG_ROADMAP.md): deliberately kinematic -- moves [playerTransform]
 * directly from input, no physics engine (per D5 in docs/MVP_PLAN.md, physics is deferred
 * to MVP1b+). Two input sources, whichever is active this frame:
 *
 * 1. Keyboard axis (desktop's GLFW polling, wasmJs's keydown/keyup listener) -- unit
 *    direction per held key, diagonals not normalized (matches this codebase's existing
 *    "simplest correct thing" bar for MVP1a).
 * 2. Touch-drag (Android/iOS, both already feed [Input.pointerDown]/[Input.pointerX]/
 *    [Input.pointerY] from their existing touch handlers -- no new platform code needed):
 *    a virtual-joystick-style drag from the touch-down origin, clamped to [DRAG_RADIUS]
 *    pixels and scaled to `[-1, 1]` per axis.
 */
class PlayerMovementSystem(
    private val playerTransform: Transform,
    private val speed: Float = DEFAULT_SPEED
) : System {
    private var dragOriginX = 0f
    private var dragOriginY = 0f
    private var wasDragging = false

    override fun update(world: World, delta: Float) {
        val (axisX, axisZ) = readMoveAxis()
        if (axisX == 0f && axisZ == 0f) {
            return
        }
        playerTransform.position.x += axisX * speed * delta
        playerTransform.position.z += axisZ * speed * delta
    }

    private fun readMoveAxis(): Pair<Float, Float> {
        var x = 0f
        var z = 0f
        if (Input.isKeyDown(Key.A) || Input.isKeyDown(Key.ArrowLeft)) x -= 1f
        if (Input.isKeyDown(Key.D) || Input.isKeyDown(Key.ArrowRight)) x += 1f
        if (Input.isKeyDown(Key.W) || Input.isKeyDown(Key.ArrowUp)) z -= 1f
        if (Input.isKeyDown(Key.S) || Input.isKeyDown(Key.ArrowDown)) z += 1f
        if (x != 0f || z != 0f) {
            wasDragging = false
            return x to z
        }
        return readDragAxis()
    }

    private fun readDragAxis(): Pair<Float, Float> {
        if (!Input.pointerDown) {
            wasDragging = false
            return 0f to 0f
        }
        if (!wasDragging) {
            dragOriginX = Input.pointerX
            dragOriginY = Input.pointerY
            wasDragging = true
            return 0f to 0f
        }
        val dx = (Input.pointerX - dragOriginX).coerceIn(-DRAG_RADIUS, DRAG_RADIUS) / DRAG_RADIUS
        val dy = (Input.pointerY - dragOriginY).coerceIn(-DRAG_RADIUS, DRAG_RADIUS) / DRAG_RADIUS
        // Screen Y grows downward; dragging up (negative dy) should move the player forward.
        return dx to dy
    }

    private companion object {
        const val DEFAULT_SPEED = 3f
        const val DRAG_RADIUS = 80f
    }
}
