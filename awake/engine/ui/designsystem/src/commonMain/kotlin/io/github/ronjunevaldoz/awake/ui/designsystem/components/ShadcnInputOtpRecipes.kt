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
import io.github.ronjunevaldoz.awake.ui.headless.box
import io.github.ronjunevaldoz.awake.ui.headless.clickable
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.requestFocus
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.textField

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
        row(horizontalArrangement = Arrangement.spacedBy(6f.dp), modifier = Modifier.height(36f.dp)) {
            repeat(length) { index ->
                if (groupSize > 0 && index > 0 && index % groupSize == 0) text("-")
                val char = value.getOrNull(index)?.toString().orEmpty()
                surface(
                    id = "$id.slot.$index",
                    modifier = Modifier.height(36f.dp).clickable { requestFocus(id) },
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
        }
    }
    val input = textField(id = id, value = value, modifier = Modifier.height(1f.dp), enabled = enabled)
    val next = input.filter(Char::isDigit).take(length)
    if (next != value) {
        resolved = next
        onValueChange(next)
    }
    return resolved
}

fun ColumnScope.shadcnInputOTP(
    id: String,
    value: String,
    length: Int = 6,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    groupSize: Int = 0,
    onValueChange: (String) -> Unit = {},
): String {
    // Keep the same visible segmented surface as the UiScope overload. The old ColumnScope
    // overload rendered only a 1dp hidden text field, which is why the showcase's OTP sample
    // appeared cropped/empty even though input state still worked.
    surface(id = "$id.slots", modifier = modifier) {
        row(horizontalArrangement = Arrangement.spacedBy(6f.dp), modifier = Modifier.height(36f.dp)) {
            repeat(length) { index ->
                if (groupSize > 0 && index > 0 && index % groupSize == 0) text("-")
                val char = value.getOrNull(index)?.toString().orEmpty()
                surface(
                    id = "$id.slot.$index",
                    modifier = Modifier.height(36f.dp).clickable { requestFocus(id) },
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
        }
    }
    val input = textField(id, value, modifier = Modifier.height(1f.dp), enabled = enabled)
    val next = input.filter(Char::isDigit).take(length)
    if (next != value) onValueChange(next)
    return next
}
