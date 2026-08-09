// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls.systems

import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.controls.systems.CameraInputSystem.Companion.DEFAULT_MODE_KEYS
import io.github.ronjunevaldoz.awake.ui.context.UiInputOwnership
import io.github.ronjunevaldoz.awake.ui.context.blocksGameplayKeys

/**
 * Switches the [CameraMode] on the [ActiveCamera] via hotkeys, so only one mode consumes
 * input and drives movement at a time.
 *
 * [modeKeys] is injectable because a hotkey is a whole-application decision, not something a
 * reusable system should claim unilaterally -- [DEFAULT_MODE_KEYS] deliberately skips
 * [Key.F3], which `GameUiRuntime` already owns for the debug overlay.
 */
class CameraInputSystem(
    private val inputProvider: () -> InputSnapshot,
    private val uiResultProvider: () -> UiInputOwnership,
    private val modeKeys: Map<Key, CameraMode> = DEFAULT_MODE_KEYS,
) : System {
    override fun update(world: World, delta: Float) {
        // The edges come from InputSnapshot, which recomputes them every frame regardless of
        // this early return -- so releasing the UI can't replay a keypress made over a widget.
        if (uiResultProvider().blocksGameplayKeys) return

        val input = inputProvider()
        for ((key, mode) in modeKeys) {
            if (input.wasPressed(key)) setCameraMode(world, mode)
        }
    }

    private fun setCameraMode(world: World, mode: CameraMode) {
        world.queryEach(CameraComponent::class, ActiveCamera::class) { _, config, _ ->
            config.mode = mode
        }
    }

    companion object {
        /** F3 is intentionally absent -- it is the engine's debug-overlay toggle. */
        val DEFAULT_MODE_KEYS: Map<Key, CameraMode> = mapOf(
            Key.F1 to CameraMode.FirstPerson,
            Key.F2 to CameraMode.ThirdPerson,
            Key.F4 to CameraMode.Cinematic,
            Key.F5 to CameraMode.TopDown,
        )
    }
}
