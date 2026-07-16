// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.reflect.KProperty

/**
 * Awake-native widget-local state holder backed by [WidgetState].
 *
 * This is the immediate-mode equivalent of a tiny `remember { mutableStateOf(...) }`, but it
 * lives inside Awake's own UI runtime and survives only for the lifetime of the owning
 * [UiContext]'s stable widget id.
 */
class UiStateValue<T> internal constructor(
    private val state: WidgetState,
    private val key: String,
    initial: () -> T
) {
    private val defaultValue: T = initial()

    var value: T
        get() = state.get(key, defaultValue)
        set(value) {
            state.set(key, value)
        }

    fun update(transform: (T) -> T) {
        value = transform(value)
    }

    fun reset() {
        state.remove(key)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class UiPopupState internal constructor(
    private val expandedState: UiStateValue<Boolean>
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
    initial: () -> T
): UiStateValue<T> = UiStateValue(this, key, initial)

fun <T> UiContext.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T
): UiStateValue<T> = widgetStateInternal(id).rememberStateValue(key, initial)

fun <T> UiScope.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T
): UiStateValue<T> = context.rememberStateValue(id, key, initial)

fun UiContext.rememberBooleanState(
    id: String,
    key: String = "value",
    initial: Boolean = false
): UiStateValue<Boolean> = rememberStateValue(id, key) { initial }

fun UiScope.rememberBooleanState(
    id: String,
    key: String = "value",
    initial: Boolean = false
): UiStateValue<Boolean> = rememberStateValue(id, key) { initial }

fun UiContext.rememberFloatState(
    id: String,
    key: String = "value",
    initial: Float = 0f
): UiStateValue<Float> = rememberStateValue(id, key) { initial }

fun UiScope.rememberFloatState(
    id: String,
    key: String = "value",
    initial: Float = 0f
): UiStateValue<Float> = rememberStateValue(id, key) { initial }

fun UiContext.rememberIntState(
    id: String,
    key: String = "value",
    initial: Int = 0
): UiStateValue<Int> = rememberStateValue(id, key) { initial }

fun UiScope.rememberIntState(
    id: String,
    key: String = "value",
    initial: Int = 0
): UiStateValue<Int> = rememberStateValue(id, key) { initial }

fun UiContext.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false
): UiPopupState = UiPopupState(rememberBooleanState(id = id, key = key, initial = initial))

fun UiScope.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false
): UiPopupState = UiPopupState(rememberBooleanState(id = id, key = key, initial = initial))
