// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

/**
 * Common subset of keys this engine cares about.
 */
enum class Key {
    W,
    A,
    S,
    D,
    ArrowUp,
    ArrowDown,
    ArrowLeft,
    ArrowRight,
    Space,
    Escape,
    F1,
    F2,
    F3,
    F4,
    F5,
}

/**
 * Discrete text-editing commands.
 */
enum class TextEditAction {
    Backspace,
    Delete,
    Enter,
    ArrowLeft,
    ArrowRight,
    ArrowUp,
    ArrowDown,
    Home,
    End,
}

/**
 * Immutable capture of the hardware state for a single frame.
 *
 * [keysDown] is level-triggered (held), [keysPressed]/[keysReleased] are the edges against the
 * previous frame. The edges live here, computed once in [Input.updateSnapshot], because every
 * consumer that hand-rolled its own `lastKeysDown` diff had to remember to refresh that copy
 * even on the frames it early-returned -- forget it once and a keypress made over a UI widget
 * replays as "just pressed" the moment the UI lets go.
 */
data class InputSnapshot(
    val pointerX: Float,
    val pointerY: Float,
    val pointerDown: Boolean,
    val scrollDeltaX: Float,
    val scrollDeltaY: Float,
    val keysDown: Set<Key>,
    /** Went down this frame (in [keysDown] now, absent from the previous frame's). */
    val keysPressed: Set<Key>,
    /** Went up this frame (in the previous frame's [keysDown], absent now). */
    val keysReleased: Set<Key>,
    val typedText: String,
    val editActions: List<TextEditAction>,
    val secondaryPointerDown: Boolean = false,
) {
    fun isDown(k: Key): Boolean = k in keysDown

    fun wasPressed(k: Key): Boolean = k in keysPressed
}

/**
 * Accumulator for polled input state.
 *
 * One instance per game session. Decoupled from global state.
 */
class Input {
    private val keysDown = mutableSetOf<Key>()
    private val typedText = StringBuilder()
    private val pendingEditActions = mutableListOf<TextEditAction>()

    var pointerDown: Boolean = false
        private set

    var pointerX: Float = 0f
        private set

    var pointerY: Float = 0f
        private set

    var scrollDeltaX: Float = 0f
    var scrollDeltaY: Float = 0f

    /** Set by the UI pass to signal focus to platform bridges (e.g. soft keyboard). */
    var textInputFocused: Boolean = false

    /** The stable hardware state for the current frame. Updated via [updateSnapshot]. */
    var currentSnapshot: InputSnapshot = InputSnapshot(
        pointerX = -1f,
        pointerY = -1f,
        pointerDown = false,
        scrollDeltaX = 0f,
        scrollDeltaY = 0f,
        keysDown = emptySet(),
        keysPressed = emptySet(),
        keysReleased = emptySet(),
        typedText = "",
        editActions = emptyList(),
    )
        private set

    /** Captures the current state into an immutable snapshot and prepares the
     * accumulator for the next frame. */
    fun updateSnapshot(): InputSnapshot {
        val previousKeysDown = currentSnapshot.keysDown
        val heldKeys = keysDown.toSet()
        currentSnapshot = InputSnapshot(
            pointerX = pointerX,
            pointerY = pointerY,
            pointerDown = pointerDown,
            scrollDeltaX = scrollDeltaX,
            scrollDeltaY = scrollDeltaY,
            keysDown = heldKeys,
            keysPressed = heldKeys - previousKeysDown,
            keysReleased = previousKeysDown - heldKeys,
            typedText = typedText.toString(),
            editActions = pendingEditActions.toList(),
            secondaryPointerDown = secondaryPointerDown,
        )
        // Clear transient buffers
        scrollDeltaX = 0f
        scrollDeltaY = 0f
        typedText.clear()
        pendingEditActions.clear()
        return currentSnapshot
    }

    /** Legacy support or internal use. Prefer [updateSnapshot]. */
    fun snapshot(): InputSnapshot = updateSnapshot()

    fun isKeyDown(key: Key): Boolean = keysDown.contains(key)

    fun pushTypedText(text: String) {
        typedText.append(text)
    }

    fun pushEditAction(action: TextEditAction) {
        pendingEditActions.add(action)
    }

    fun setKeyDown(key: Key, down: Boolean) {
        if (down) keysDown.add(key) else keysDown.remove(key)
    }

    var secondaryPointerDown: Boolean = false
        private set

    fun setSecondaryPointer(down: Boolean) {
        secondaryPointerDown = down
    }

    fun setPointer(down: Boolean, x: Float, y: Float) {
        pointerDown = down
        pointerX = x
        pointerY = y
    }

    fun clearKeys() {
        keysDown.clear()
    }
}
