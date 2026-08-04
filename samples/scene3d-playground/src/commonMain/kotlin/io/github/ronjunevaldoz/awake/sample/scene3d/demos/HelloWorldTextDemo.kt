// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** Simplest possible playground entry -- proves the menu/viewport/controls wiring with plain
 * 2D text instead of any real 3D content. Every other demo in this package follows this same
 * shape: local mutable state up top, [renderViewport] reads it, [renderControls] writes it. */
internal object HelloWorldTextDemo {
    private var fontSizeDp = 28f
    private var centered = true

    val entry = Scene3DDemo(
        id = "hello-world-text",
        title = "Hello world text",
        renderViewport = {
            text(
                label = "Hello, Awake!",
                modifier = Modifier.fillMaxWidth().height((fontSizeDp * 1.4f).dp),
                color = Color(0.15f, 0.13f, 0.36f, 1f),
                centered = centered,
                // The modifier's height only sizes the slot text is centered within -- it never
                // scaled the glyphs themselves, so dragging the slider just moved "Hello, Awake!"
                // up/down inside a taller/shorter box instead of actually growing/shrinking it
                // (the reported bug). Real size comes from textStyle.size; height above is scaled
                // 1.4x that so there's still headroom to vertically center a tall font size.
                textStyle = TextStyle(size = fontSizeDp.sp)
            )
        },
        renderControls = {
            shadcnSurface(id = "hello-world-controls-panel", modifier = Modifier.fillMaxWidth()) {
                fontSizeDp = shadcnFieldSliderWithValue(
                    id = "hello-world-font-size",
                    label = "Font size",
                    min = 12f,
                    max = 64f,
                    value = fontSizeDp
                )
                centered = shadcnSwitch(
                    id = "hello-world-centered",
                    checked = centered,
                    label = "Centered"
                )
            }
        }
    )
}
