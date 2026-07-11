// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

import kotlin.concurrent.Volatile
import kotlin.native.concurrent.ThreadLocal

/**
 * Common subset of keys this engine cares about -- deliberately small (movement + a couple
 * of demo/debug keys), not a 1:1 mirror of GLFW's or Android's full key space. Extend as
 * real gameplay needs more, rather than pre-mapping everything up front.
 */
enum class Key {
    W, A, S, D,
    ArrowUp, ArrowDown, ArrowLeft, ArrowRight,
    Space, Escape
}

/**
 * Polled input state, read once per frame from [io.github.ronjunevaldoz.awake.core
 * .application.Application.update] (or a system it calls) -- not callback-driven. This
 * matches the threading model already in place: GLFW callbacks fire synchronously inside
 * `glfwPollEvents()`, which already runs on the single render thread (see this project's
 * `.claude/AGENTS.md` "Threading model" section), so writing straight into these `@Volatile`
 * fields from a platform callback and reading them from the same thread's `update()` call
 * needs no further synchronization. Android touch events arrive on the UI thread instead --
 * those fields are `@Volatile` specifically so a render-thread read can't tear against a
 * concurrent UI-thread write, even though both threads only ever *replace* a value rather
 * than read-modify-write it.
 *
 * A single global object (matching the existing [io.github.ronjunevaldoz.awake.core.utils
 * .Time]/[io.github.ronjunevaldoz.awake.core.utils.Frame] convention in this codebase) --
 * this engine runs one [io.github.ronjunevaldoz.awake.core.graphics.Application] instance
 * per process, so there's no real multi-window/multi-input-source case to model yet.
 */
@ThreadLocal
object Input {
    private val keysDown = HashSet<Key>()

    @Volatile
    var pointerDown: Boolean = false
        private set

    @Volatile
    var pointerX: Float = 0f
        private set

    @Volatile
    var pointerY: Float = 0f
        private set

    fun isKeyDown(key: Key): Boolean = keysDown.contains(key)

    /** Called only by platform input callbacks (GLFW key callback, Android
     * `onTouchEvent`-fed key events, if any) -- not part of the read-side API a game
     * system should call. */
    fun setKeyDown(key: Key, down: Boolean) {
        if (down) keysDown.add(key) else keysDown.remove(key)
    }

    /** Called only by platform input callbacks (GLFW cursor-pos/mouse-button callbacks,
     * Android `onTouchEvent`). [x]/[y] are in the same pixel coordinate space
     * [io.github.ronjunevaldoz.awake.core.utils.Frame] already uses. */
    fun setPointer(down: Boolean, x: Float, y: Float) {
        pointerDown = down
        pointerX = x
        pointerY = y
    }

    /** Clears all held-key state -- call when the window/surface loses focus (GLFW
     * `glfwSetWindowFocusCallback`, Android `surfaceDestroyed`) so a key release that
     * happens while unfocused doesn't leave a stuck "still down" key. Not wired to a
     * focus callback yet (no such callback exists on either platform today); exposed so
     * the first caller that adds one has somewhere correct to call. */
    fun clearKeys() {
        keysDown.clear()
    }
}
