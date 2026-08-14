// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.headless.box
import io.github.ronjunevaldoz.awake.ui.headless.clickable
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.requestFocus
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.textField
import io.github.ronjunevaldoz.awake.ui.headless.uiScope

import io.github.ronjunevaldoz.awake.ui.api.layout.tw

fun UiScope.shadcnInputOTP(
    id: String,
    value: String,
    length: Int = 6,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    groupSize: Int = 0,
    onValueChange: (String) -> Unit = {},
): String {
    var resolved = value
    surface(id = "$id.slots", modifier = modifier) {
        row(horizontalArrangement = Arrangement.spacedBy(1.5.tw), modifier = Modifier.height(9.tw)) {
            repeat(length) { index ->
                if (groupSize > 0 && index > 0 && index % groupSize == 0) text("-")
                val char = value.getOrNull(index)?.toString().orEmpty()
                surface(
                    id = "$id.slot.$index",
                    modifier = Modifier.height(9.tw).width(9.tw).clickable { requestFocus(id) },
                    style = SurfaceStyle(
                        background = themeValues.colors.card,
                        foreground = if (enabled) themeValues.colors.foreground else themeValues.colors.mutedForeground,
                        border = SurfaceBorder(1f.dp, if (isError) themeValues.colors.destructive else themeValues.colors.input),
                        cornerRadius = themeValues.shapes.md,
                    ),
                ) {
                    box(modifier = Modifier.fillMaxSize(), contentAlignment = UiAlignment.Center) {
                        text(char, centered = true)
                    }
                }
            }
            // The backing field only carries keystrokes and focus -- the slots above are what the
            // user reads. Its own text must be invisible too, not just its box: at 1dp wide it
            // still painted the full value (a 88px Text node at the row's trailing edge, clipped
            // by the container to a ~2px smudge right of the last slot). Transparent foreground
            // covers the text and the caret, both of which the slots already represent.
            val input = textField(
                id = id,
                value = value,
                modifier = Modifier.height(9.tw).width(1f.dp),
                visuals = io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals(
                    rest = SurfaceStyle(
                        background = io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                        foreground = io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                        border = SurfaceBorder(0f.dp, io.github.ronjunevaldoz.awake.core.colors.Color.Transparent),
                    ),
                ),
                enabled = enabled,
            )
            val next = input.filter(Char::isDigit).take(length)
            if (next != value) {
                resolved = next
                onValueChange(next)
            }
        }
    }
    return resolved
}
