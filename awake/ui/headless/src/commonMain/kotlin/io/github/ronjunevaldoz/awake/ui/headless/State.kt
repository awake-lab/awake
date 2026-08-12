// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.UiPopupState
import kotlin.reflect.KProperty
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState as rememberPrimitiveBooleanState
import io.github.ronjunevaldoz.awake.ui.rememberPopupState as rememberPrimitivePopupState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue as rememberPrimitiveStateValue

/** Headless state holder; the Core storage delegate stays behind this facade. */
class UiStateValue<T> internal constructor(
    private val delegate: io.github.ronjunevaldoz.awake.ui.UiStateValue<T>,
) {
    var value: T
        get() = delegate.value
        set(value) {
            delegate.value = value
        }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        value = newValue
    }
}

/** Public popup state contract for Headless recipes and design-system adapters. */
fun UiScope.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false,
): UiPopupState = primitive.rememberPrimitivePopupState(id, key, initial)

fun <T> UiScope.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T,
): UiStateValue<T> = UiStateValue(primitive.rememberPrimitiveStateValue(id, key, initial))

fun UiScope.rememberBooleanState(
    id: String,
    key: String = "value",
    initial: Boolean = false,
): UiStateValue<Boolean> = UiStateValue(primitive.rememberPrimitiveBooleanState(id, key, initial))

fun <T> ColumnScope.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T,
): UiStateValue<T> = UiStateValue(primitive.rememberPrimitiveStateValue(id, key, initial))

fun ColumnScope.rememberBooleanState(
    id: String,
    key: String = "value",
    initial: Boolean = false,
): UiStateValue<Boolean> = UiStateValue(primitive.rememberPrimitiveBooleanState(id, key, initial))

fun <T> RowScope.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T,
): UiStateValue<T> = UiStateValue(primitive.rememberPrimitiveStateValue(id, key, initial))

fun RowScope.rememberBooleanState(
    id: String,
    key: String = "value",
    initial: Boolean = false,
): UiStateValue<Boolean> = UiStateValue(primitive.rememberPrimitiveBooleanState(id, key, initial))

fun <T> BoxScope.rememberStateValue(
    id: String,
    key: String = "value",
    initial: () -> T,
): UiStateValue<T> = UiStateValue(primitive.rememberPrimitiveStateValue(id, key, initial))

fun ColumnScope.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false,
): UiPopupState = primitive.rememberPrimitivePopupState(id, key, initial)

fun RowScope.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false,
): UiPopupState = primitive.rememberPrimitivePopupState(id, key, initial)
