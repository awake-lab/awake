// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls.systems

import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.controls.components.MovementControl
import io.github.ronjunevaldoz.awake.ui.context.UiInputOwnership
import io.github.ronjunevaldoz.awake.ui.context.blocksGameplayKeys
import kotlin.math.sqrt

/**
 * Dedicated system for handling user input and mapping it to [MovementControl] intent.
 * This is the ONLY system that should read/consume hardware snapshots for movement.
 */
class PlayerInputSystem(
    /** Provider for the hardware snapshot for the current frame. */
    private val inputProvider: () -> InputSnapshot,
    /** Provider for the UI's input consumption results. */
    private val uiResultProvider: () -> UiInputOwnership,
) : System {
    override fun update(world: World, delta: Float) {
        val input = inputProvider()
        val ui = uiResultProvider()

        // If the UI owns the pointer or is taking text, clear all movement intents -- typing
        // "wasd" into a focused field must not also walk the player.
        if (ui.blocksGameplayKeys) {
            world.queryEach(MovementControl::class) { _, control ->
                control.moveX = 0f
                control.moveZ = 0f
            }
            return
        }

        var moveX = 0f
        var moveZ = 0f
        if (input.keysDown.contains(Key.W) || input.keysDown.contains(Key.ArrowUp)) moveZ += 1f
        if (input.keysDown.contains(Key.S) || input.keysDown.contains(Key.ArrowDown)) moveZ -= 1f
        if (input.keysDown.contains(Key.A) || input.keysDown.contains(Key.ArrowLeft)) moveX -= 1f
        if (input.keysDown.contains(Key.D) || input.keysDown.contains(Key.ArrowRight)) moveX += 1f

        // Normalize vector to prevent diagonal speed boost
        if (moveX != 0f || moveZ != 0f) {
            val len = sqrt(moveX * moveX + moveZ * moveZ)
            moveX /= len
            moveZ /= len
        }

        world.queryEach(MovementControl::class) { _, control ->
            control.moveX = moveX
            control.moveZ = moveZ
        }
    }
}
