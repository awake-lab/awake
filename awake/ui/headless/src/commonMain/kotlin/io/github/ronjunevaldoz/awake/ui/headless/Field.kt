// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Form field context passed down to child controls and accessibility helpers. */
data class FieldState(
    val id: String,
    val label: String? = null,
    val description: String? = null,
    val error: String? = null,
    val isInvalid: Boolean = error != null,
    val isDisabled: Boolean = false,
)

/** Receiver scope for building form field compositions. */
class FieldScope(
    val field: FieldState,
    private val scope: UiScope,
) : UiScope by scope

/**
 * Unstyled Form Field wrapper managing label, description, error state, and accessibility context.
 */
fun UiScope.field(
    id: String,
    label: String? = null,
    description: String? = null,
    error: String? = null,
    disabled: Boolean = false,
    modifier: Modifier = Modifier,
    labelStyle: Style = Style.Empty,
    descriptionStyle: Style = Style.Empty,
    errorStyle: Style = Style.Empty,
    content: FieldScope.() -> Unit,
): UiBounds = column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4f.dp),
) {
    val fieldState = FieldState(
        id = id,
        label = label,
        description = description,
        error = error,
        isInvalid = error != null,
        isDisabled = disabled,
    )

    if (label != null) {
        text(
            label = label,
            style = labelStyle,
        )
    }

    FieldScope(fieldState, this).content()

    if (error != null) {
        text(
            label = error,
            style = errorStyle,
        )
    } else if (description != null) {
        text(
            label = description,
            style = descriptionStyle,
        )
    }
}
