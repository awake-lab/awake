// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnInput
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.box
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.clickable
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.scope.isFocused
import io.github.ronjunevaldoz.awake.ui.scope.requestFocus
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.components.otpActiveSlotIndex
import io.github.ronjunevaldoz.awake.ui.unstyled.components.otpDigitsOnly
import io.github.ronjunevaldoz.awake.ui.unstyled.components.otpShowsSeparatorBefore
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/**
 * Real 1:1 shadcn/ui `InputOTP`: a segmented One-Time Password / PIN code digit entry row
 * rendering individual rounded square slot boxes (`[ ] [ ] [ ] [ ] [ ] [ ]`).
 *
 * Architecture:
 * - A visual row of fixed 36×36 dp slot boxes draws first (shadcn's `h-9 w-9`). Each slot shows
 *   one digit centered within the full slot bounds via the explicit-slot [text] overload.
 * - A transparent, zero-height [shadcnInput] is rendered **on top** (z-order 1) so it is the
 *   first hit-test candidate for pointer events.  Clicking any slot of the visual row is
 *   handled by the overlay input which captures the pointer and calls [requestFocus] on itself.
 * - Each visual slot also has a `.clickable { requestFocus(id) }` fallback for the gap pixels
 *   between the transparent input and the slot borders.
 */
fun UiScope.shadcnInputOTP(
    id: String,
    value: String,
    length: Int = 6,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    groupSize: Int = 0,
    onValueChange: (String) -> Unit = {},
): String {
    var resultValue = value
    val focused = enabled && isFocused(id)
    val shadcnResolvedTheme = theme.asShadcnTheme()
    val palette = shadcnResolvedTheme.palette
    val activeSlotIndex = otpActiveSlotIndex(focused, resultValue.length, length)

    box(modifier = modifier.height(Tw.Spacing.s9)) {
        // 1. Visual slot row drawn FIRST (z-order 0). Each slot:
        //    - has a clickable fallback that routes focus to the hidden shadcnInput
        //    - renders its digit centered within the full slot bounds via text(slot=...)
        //
        row(
            horizontalArrangement = Arrangement.spacedBy(6f.dp),
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().height(Tw.Spacing.s9),
        ) {
            for (i in 0 until length) {
                if (otpShowsSeparatorBefore(i, groupSize)) {
                    // Separator between groups (e.g. "123 - 456")
                    text("-", color = palette.mutedForeground)
                }

                val char = resultValue.getOrNull(i)?.toString() ?: ""
                val isActiveSlot = i == activeSlotIndex
                val slotBorderColor = when {
                    isError -> palette.destructive
                    isActiveSlot -> palette.ring
                    else -> palette.border
                }

                surface(
                    id = "$id.slot.$i",
                    modifier = Modifier
                        .width(Tw.Spacing.s9)
                        .height(Tw.Spacing.s9)
                        .clickable(enabled = enabled) { requestFocus(id) },
                    style = Style {
                        background(palette.card)
                        border(if (isActiveSlot || isError) 1.5f.dp else 1f.dp, slotBorderColor)
                        // Real shadcn's InputOTP slot corners are rounded-md, drawn from the
                        // active theme's radius scale instead of a bare literal.
                        shape(shadcnResolvedTheme.radii.md)
                        // Zero content padding so the slot slot-lambda receives the full 36×36 area
                        contentPadding(0f.dp)
                    },
                ) { slotBounds ->
                    // Draw the digit centered within the full slotBounds (not a wrap-content
                    // claim). text(slot=...) renders directly into the given bounds without
                    // claiming a new layout slot, giving perfect horizontal + vertical centering.
                    // BasicText pixel-snaps the emitted glyph quad itself, so sub-pixel drift
                    // cannot shift the char up or down.
                    if (char.isNotEmpty()) {
                        text(
                            label = char,
                            slot = slotBounds,
                            color = if (enabled) palette.foreground else palette.mutedForeground,
                            centered = true,
                            verticallyCentered = true,
                        )
                    }
                }
            }
        }

        // 2. Transparent shadcnInput rendered ON TOP (z-order 1) so it is the first
        //    hit-test candidate.  It owns all keyboard, focus, and IME events. It is
        //    rendered invisible (transparent fg/bg/border) so nothing is visually visible,
        //    but pointer-down events land here first, establishing focus without needing
        //    the per-slot clickable to explicitly call requestFocus.
        val rawInput = shadcnInput(
            id = id,
            value = value,
            enabled = enabled,
            isError = isError,
            modifier = Modifier.fillMaxWidth().height(Tw.Spacing.s9),
            style = Style {
                foreground(Color.Transparent)
                background(Color.Transparent)
                border(0f.dp, Color.Transparent)
            },
        )

        val digitsOnly = otpDigitsOnly(rawInput, length)
        if (digitsOnly != value) {
            resultValue = digitsOnly
            onValueChange(resultValue)
        }
    }

    return resultValue
}
