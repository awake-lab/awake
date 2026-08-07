// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.systems.CameraInputSystem
import io.github.ronjunevaldoz.awake.scene.systems.PlayerInputSystem
import io.github.ronjunevaldoz.awake.ui.context.UiInputOwnership
import io.github.ronjunevaldoz.awake.ui.context.blocksGameplayKeys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the defect where typing into a focused text field also drove the game: ownership was
 * checked with `isCaptured`, which is pointer capture only, so "wasd" both inserted characters
 * and walked the player while the camera hotkeys kept firing.
 */
class TextFocusBlocksGameplayTest {
    @Test
    fun blocksGameplayKeysCoversPointerCaptureAndTextFocus() {
        assertFalse(UiInputOwnership().blocksGameplayKeys, "an idle UI must not block gameplay")
        assertTrue(UiInputOwnership(isCaptured = true).blocksGameplayKeys)
        assertTrue(UiInputOwnership(isTextInputFocused = true).blocksGameplayKeys)
        assertTrue(
            UiInputOwnership(isCaptured = true, isTextInputFocused = true).blocksGameplayKeys,
        )
    }

    @Test
    fun typingIntoAFocusedFieldDoesNotMoveThePlayer() {
        val world = World()
        val subject = world.create()
        world.add(subject, MovementControl().apply { moveZ = 1f })

        PlayerInputSystem(
            inputProvider = { snapshotWith(Key.W) },
            uiResultProvider = { UiInputOwnership(isTextInputFocused = true) },
        ).update(world, DELTA)

        val control = world.get(subject, MovementControl::class)!!
        assertEquals(0f, control.moveZ, "W typed into a text field must not walk the player")
        assertEquals(0f, control.moveX)
    }

    @Test
    fun holdingWithoutUiOwnershipStillMovesThePlayer() {
        val world = World()
        val subject = world.create()
        world.add(subject, MovementControl())

        PlayerInputSystem(
            inputProvider = { snapshotWith(Key.W) },
            uiResultProvider = { UiInputOwnership() },
        ).update(world, DELTA)

        assertEquals(1f, world.get(subject, MovementControl::class)!!.moveZ)
    }

    @Test
    fun cameraHotkeyIsIgnoredWhileTextInputIsFocused() {
        val world = World()
        val config = activeCamera(world)
        var focused = true

        val input = Input()
        val system = CameraInputSystem(
            inputProvider = { input.currentSnapshot },
            uiResultProvider = { UiInputOwnership(isTextInputFocused = focused) },
        )

        input.setKeyDown(Key.F2, down = true)
        input.updateSnapshot()
        system.update(world, DELTA)
        assertEquals(CameraMode.FirstPerson, config.mode, "F2 typed into a field must not switch mode")

        // Releasing focus while the key is still held must not replay the press: the edge was
        // spent on the blocked frame, which is exactly what the deleted lastKeysDown copy had
        // to remember to do by hand.
        focused = false
        input.updateSnapshot()
        system.update(world, DELTA)
        assertEquals(CameraMode.FirstPerson, config.mode, "a still-held key must not re-fire")

        input.setKeyDown(Key.F2, down = false)
        input.updateSnapshot()
        input.setKeyDown(Key.F2, down = true)
        input.updateSnapshot()
        system.update(world, DELTA)
        assertEquals(CameraMode.ThirdPerson, config.mode, "a fresh press must switch mode")
    }

    private fun activeCamera(world: World): CameraComponent {
        val entity = world.create()
        val config = CameraComponent()
        world.add(entity, config)
        world.add(entity, ActiveCamera())
        return config
    }

    private fun snapshotWith(vararg keys: Key): InputSnapshot = InputSnapshot(
        pointerX = 0f,
        pointerY = 0f,
        pointerDown = false,
        scrollDeltaX = 0f,
        scrollDeltaY = 0f,
        keysDown = keys.toSet(),
        keysPressed = keys.toSet(),
        keysReleased = emptySet(),
        typedText = "",
        editActions = emptyList(),
    )

    private companion object {
        const val DELTA = 0.016f
    }
}
