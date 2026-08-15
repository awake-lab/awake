// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.ui.api.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * State-neutral base colors. Interaction-state styling (hover/pressed/disabled) is owned by
 * the widget's caller-supplied style, never by theme defaults: a default state rule outranks
 * a caller's unconditional fill under state-conditional resolution, which is how every filled
 * shadcn button variant turned muted-gray on hover (2026-08-15 audit).
 */
fun UiColorTokens.neutralStyle(): Style =
    Style {
        background(background)
        foreground(foreground)
    }
