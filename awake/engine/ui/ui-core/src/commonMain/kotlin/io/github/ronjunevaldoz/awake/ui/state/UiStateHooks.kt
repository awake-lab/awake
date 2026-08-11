// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import kotlin.reflect.KProperty

/**
 * Awake-native widget-local state holder backed by [WidgetState].
 *
 * This is the immediate-mode equivalent of a tiny `remember { mutableStateOf(...) }`, but it
 * lives inside Awake's own UI runtime and survives only for the lifetime of the owning
 * [io.github.ronjunevaldoz.awake.ui.context.UiContext]'s stable widget id.
 */
class UiStateValue<T> internal constructor(
    private val state: WidgetState,
    private val key: String,
    initial: () -> T,
    /**
     * True while a trial measuring pass is running, in which case writes are DROPPED.
     *
     * `column()` re-executes its content against a scratch context that shares this real,
     * persisted [WidgetState] but starts from blank input. Anything that writes state from that
     * pass corrupts the value the real pass reads moments later, in the same frame. That has
     * shipped three times: the resizable handle's drag anchor was deleted every frame,
     * `animatedHeight` cached a stale height, and a popup nested in an extra column would not
     * open because its toggle was clobbered.
     *
     * Suppressing at the setter fixes every hook built on this at once -- popup, boolean, float,
     * scroll -- rather than each widget remembering to guard itself, which is what the previous
     * per-widget `isMeasuring()` gates required and what kept letting new cases through.
     */
    private val isMeasuring: () -> Boolean = { false },
) {
    private val defaultValue: T = initial()

    var value: T
        get() = state.get(key, defaultValue)
        set(value) {
            if (isMeasuring()) return
            state.set(key, value)
        }

    fun update(transform: (T) -> T) {
        value = transform(value)
    }

    fun reset() {
        if (isMeasuring()) return
        state.remove(key)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class UiPopupState internal constructor(
    private val expandedState: UiStateValue<Boolean>,
) {
    var expanded: Boolean
        get() = expandedState.value
        set(value) {
            expandedState.value = value
        }

    fun open() {
        expanded = true
    }

    fun close() {
        expanded = false
    }

    fun toggle() {
        expanded = !expanded
    }
}

fun <T> WidgetState.rememberStateValue(
    key: String = "value",
    initial: () -> T,
): UiStateValue<T> = UiStateValue(this, key, initial)

fun <T> UiContext.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T,
): UiStateValue<T> = UiStateValue(widgetStateInternal(id), key, initial) { isMeasuringInternal() }

fun <T> UiPrimitiveScope.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T,
): UiStateValue<T> = context.rememberStateValue(id, key, initial)

fun UiContext.rememberBooleanState(
    id: String,
    key: String = "value",
    initial: Boolean = false,
): UiStateValue<Boolean> = rememberStateValue(id, key) { initial }

fun UiPrimitiveScope.rememberBooleanState(
    id: String,
    key: String = "value",
    initial: Boolean = false,
): UiStateValue<Boolean> = rememberStateValue(id, key) { initial }

fun UiContext.rememberFloatState(
    id: String,
    key: String = "value",
    initial: Float = 0f,
): UiStateValue<Float> = rememberStateValue(id, key) { initial }

fun UiPrimitiveScope.rememberFloatState(
    id: String,
    key: String = "value",
    initial: Float = 0f,
): UiStateValue<Float> = rememberStateValue(id, key) { initial }

fun UiContext.rememberIntState(
    id: String,
    key: String = "value",
    initial: Int = 0,
): UiStateValue<Int> = rememberStateValue(id, key) { initial }

fun UiPrimitiveScope.rememberIntState(
    id: String,
    key: String = "value",
    initial: Int = 0,
): UiStateValue<Int> = rememberStateValue(id, key) { initial }

fun UiContext.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false,
): UiPopupState = UiPopupState(rememberBooleanState(id = id, key = key, initial = initial))

fun UiPrimitiveScope.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false,
): UiPopupState = UiPopupState(rememberBooleanState(id = id, key = key, initial = initial))

fun UiContext.rememberScrollState(
    id: String,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
): UiScrollState = PersistedUiScrollState(widgetStateInternal(id), initialOffsetX, initialOffsetY)

fun UiPrimitiveScope.rememberScrollState(
    id: String,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
): UiScrollState = context.rememberScrollState(id, initialOffsetX, initialOffsetY)
