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
 * Discrete text-editing commands a text-input widget reacts to -- deliberately separate from
 * [Key]: [Key] models held-down gameplay state polled every frame, while these are edge-
 * triggered pulses (one per physical key press, with the platform's own OS-level key-repeat
 * cadence while held) pushed through [Input.pushEditAction]/drained via
 * [Input.consumeEditActions]. Mixing "the M key is down" (gameplay) with "insert the letter M
 * at the cursor" (text editing, needs shift-state/layout awareness the [Key] enum was never
 * meant to carry -- see its own doc comment) into one enum would force every gameplay
 * consumer to also handle a few dozen printable-character entries it has no use for.
 */
enum class TextEditAction {
    Backspace, Delete, Enter, ArrowLeft, ArrowRight, Home, End
}

/**
 * Polled input state, read once per frame from [io.github.ronjunevaldoz.awake.core
 * .application.Application.update] (or a system it calls) -- not callback-driven. This
 * matches the threading model already in place: GLFW callbacks fire synchronously inside
 * `glfwPollEvents()`, which already runs on the single render thread (see
 * `docs/architecture.md`'s threading-model rules), so writing straight into these
 * `@Volatile` fields from a platform callback and reading them from the same thread's
 * `update()` call needs no further synchronization. Android touch events arrive on the UI
 * thread instead -- those fields are `@Volatile` specifically so a render-thread read can't
 * tear against a concurrent UI-thread write, even though both threads only ever *replace* a
 * value rather than read-modify-write it.
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

    /** Set by [io.github.ronjunevaldoz.awake.ui.UiContext] (not a platform input callback,
     * unlike every other field here) whenever a UI widget's `activeId` is non-null this
     * frame -- i.e. some widget already claimed the current click/drag. Scene-facing pointer
     * consumers that derive a drag delta from [pointerDown]/[pointerX]/[pointerY]
     * ([io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem]/
     * [io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem]) should treat the
     * pointer as "not really down" for drag purposes while this is `true`, so dragging a
     * slider/button doesn't simultaneously drag the orbit/free-fly camera underneath it.
     * `@Volatile` for the same cross-thread-write reason as [pointerDown] et al., even though
     * in practice the UI runs on the same thread that reads this. Public `var` (not
     * `private set`) since [UiContext] -- a different module -- is the writer, not a
     * platform callback living in this same file. */
    @Volatile
    var pointerCapturedByUi: Boolean = false

    /** Accumulated scroll/pinch delta along GLFW's `yoffset` axis (trackpad pinch surfaces
     * through GLFW's scroll callback on macOS, with a different feel than a mouse wheel but
     * the same callback/API), since the last [consumeScrollDeltaY] call. Unlike
     * [pointerX]/[pointerY] (absolute, re-polled every frame), this is event-driven: GLFW's
     * scroll callback only fires on an actual scroll/pinch tick, so deltas must accumulate
     * here between polls rather than being overwritten, or a single pinch tick landing
     * between two polls would be lost. `@Volatile` for the same cross-thread-write reason as
     * the other fields in this object, even though in practice the GLFW scroll callback
     * fires synchronously inside `glfwPollEvents()` on the same render thread that later
     * reads it (see the class doc comment). Not `private set` -- the platform polling
     * function (`pollDesktopInput` in `samples/hello-cube`) is the writer here, same as
     * [setPointer]/[setKeyDown] elsewhere in this file, just expressed as a plain field
     * instead of a setter function since it accumulates rather than replaces. */
    @Volatile
    var scrollDeltaY: Float = 0f

    /** Drains [scrollDeltaY]: returns the value accumulated since the last call and resets it
     * to 0f. Exactly one reader should call this per frame (see [OrbitCameraSystem]/
     * [FreeFlyCameraSystem] -- only one of the two runs its `update()` in a given frame per
     * the active [io.github.ronjunevaldoz.awake.scene.systems] camera-mode toggle, but this
     * consume-once contract lives here, in [Input] itself, rather than being a coordination
     * responsibility each camera system has to remember, so it stays correct even if that
     * assumption ever changes. */
    fun consumeScrollDeltaY(): Float {
        val value = scrollDeltaY
        scrollDeltaY = 0f
        return value
    }

    fun isKeyDown(key: Key): Boolean = keysDown.contains(key)

    private val typedText = StringBuilder()
    private val pendingEditActions = ArrayList<TextEditAction>()

    /** Called only by platform input bridges -- appends decoded printable text (a single
     * character, or a short run if the platform hands over more than one at once) typed
     * since the last [consumeTypedText] call. Accumulates rather than replaces, same reason
     * as [scrollDeltaY]: a keystroke landing between two polls must not be dropped. */
    fun pushTypedText(text: String) {
        typedText.append(text)
    }

    /** Drains [pushTypedText]'s buffer: returns everything typed since the last call and
     * resets it to empty. A focused text-input widget should call this exactly once per
     * frame. */
    fun consumeTypedText(): String {
        val value = typedText.toString()
        typedText.clear()
        return value
    }

    /** Called only by platform input bridges -- queues one discrete editing command (see
     * [TextEditAction]'s doc comment for why this is separate from [setKeyDown]). */
    fun pushEditAction(action: TextEditAction) {
        pendingEditActions.add(action)
    }

    /** Drains [pushEditAction]'s queue in the order the platform pushed them. A focused
     * text-input widget should call this exactly once per frame. */
    fun consumeEditActions(): List<TextEditAction> {
        if (pendingEditActions.isEmpty()) return emptyList()
        val value = pendingEditActions.toList()
        pendingEditActions.clear()
        return value
    }

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
