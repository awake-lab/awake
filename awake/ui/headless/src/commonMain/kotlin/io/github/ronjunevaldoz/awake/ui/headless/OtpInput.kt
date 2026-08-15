// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.otpDigitsOnly as primitiveOtpDigitsOnly
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.otpShowsSeparatorBefore as primitiveOtpShowsSeparatorBefore

/**
 * Generic OTP/PIN input behavior: one focus-carrying hidden text field driving [length] digit
 * slots. Owns value filtering, focus capture, and group-separator placement; the caller
 * supplies each slot's and separator's appearance -- no visual opinion lives here.
 */
fun UiScope.otpInput(
    id: String,
    value: String,
    length: Int,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier.width(1f.dp),
    enabled: Boolean = true,
    groupSize: Int = 0,
    horizontalArrangement: Arrangement = Arrangement.Start,
    onValueChange: (String) -> Unit = {},
    separator: (RowScope.() -> Unit)? = null,
    slotContent: RowScope.(index: Int, char: String) -> Unit,
): String {
    var resolved = value
    row(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        repeat(length) { index ->
            if (separator != null && primitiveOtpShowsSeparatorBefore(index, groupSize)) separator()
            slotContent(index, value.getOrNull(index)?.toString().orEmpty())
        }
        // The backing field only carries keystrokes and focus -- the slots above are what the
        // user reads, so its own text/caret must stay invisible rather than just its box.
        val input = textField(
            id = id,
            value = value,
            modifier = fieldModifier,
            visuals = SurfaceVisuals(
                rest = SurfaceStyle(
                    background = Color.Transparent,
                    foreground = Color.Transparent,
                    border = SurfaceBorder(0f.dp, Color.Transparent),
                ),
            ),
            enabled = enabled,
        )
        val next = primitiveOtpDigitsOnly(input, length)
        if (next != value) {
            resolved = next
            onValueChange(next)
        }
    }
    return resolved
}
