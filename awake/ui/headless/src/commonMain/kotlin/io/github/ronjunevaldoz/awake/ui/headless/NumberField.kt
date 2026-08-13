// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/** Scope receiver providing increment/decrement button triggers and input value binding. */
class NumberFieldScope(
    val value: Float,
    val onIncrement: () -> Unit,
    val onDecrement: () -> Unit,
    val onValueChange: (Float) -> Unit,
    private val scope: UiScope,
) : UiScope by scope

/**
 * Unstyled NumberField primitive providing numeric stepper value, step calculations, and increment/decrement actions.
 */
fun UiScope.numberField(
    id: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float = Float.NEGATIVE_INFINITY,
    max: Float = Float.POSITIVE_INFINITY,
    step: Float = 1f,
    modifier: Modifier = Modifier,
    content: NumberFieldScope.() -> Unit,
): UiBounds = row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4f.dp),
) {
    val increment = {
        val next = (value + step).coerceIn(min, max)
        if (next != value) onValueChange(next)
    }
    val decrement = {
        val next = (value - step).coerceIn(min, max)
        if (next != value) onValueChange(next)
    }

    val numberScope = NumberFieldScope(
        value = value,
        onIncrement = increment,
        onDecrement = decrement,
        onValueChange = { nextVal -> onValueChange(nextVal.coerceIn(min, max)) },
        scope = this,
    )
    numberScope.content()
}
