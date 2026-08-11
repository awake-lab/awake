// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test

/** THROWAWAY probe -- not meant to stay in the suite; just prints real measured numbers. */
class TypographyPaddingProbeTest {

    @Test
    fun measureSingleLineTextClaimedHeightVsInkHeight() {
        val ui = UiContext()
        ui.beginFrame(400f, 400f, testSnapshot())
        val claimed: UiBounds = ui.createColumn(x = 0f, y = 0f, width = 400f)
            .text("Hello", style = Style { textSize(14f.sp) })
        ui.endFrame()
        val nodes = ui.semanticNodes()
        val node = nodes.first { it.label == "Hello" }
        println("=== fontSize=14dp (via style{textSize}) ===")
        println("claimed slot bounds (returned) = $claimed")
        println("semantic node.bounds (slot passed to renderTextBlock, post content-padding inset) = ${node.bounds}")
        println("semantic node.contentBounds (real glyph ink bbox) = ${node.contentBounds}")
        println("semantic node.clippedBounds = ${node.clippedBounds}")
    }

    /** Demonstrates the dead `textStyle` PARAMETER on the modifier-based text() overload:
     *  passing it directly is silently ignored (shadowed by a same-named local variable
     *  inside the function body) -- font size stays pinned to theme.typography.body (16sp)
     *  no matter what size is requested here. */
    @Test
    fun textStyleParameterOnModifierOverloadIsIgnored() {
        val ui = UiContext()
        ui.beginFrame(400f, 400f, testSnapshot())
        val bounds: UiBounds = ui.createColumn(x = 0f, y = 0f, width = 400f)
            .text("Hello", textStyle = TextStyle(size = 40f.sp))
        ui.endFrame()
        println("=== requested textStyle(size=40sp) via dead param ===")
        println("actual bounds = $bounds (expected height around 40, got theme default instead)")
    }

    @Test
    fun measureAcrossFontSizes() {
        listOf(12f, 14f, 16f, 20f, 24f).forEach { size ->
            val ui = UiContext()
            ui.beginFrame(400f, 400f, testSnapshot())
            ui.createColumn(x = 0f, y = 0f, width = 400f)
                .text("Ag", style = Style { textSize(size.sp) })
            ui.endFrame()
            val node = ui.semanticNodes().first { it.label == "Ag" }
            println(
                "fontSize=${size}sp (via style{textSize}) -> node.bounds.height=${node.bounds.height} " +
                    "contentBounds.height=${node.contentBounds?.height} " +
                    "ratio(claimed/fontSize)=${node.bounds.height / size} " +
                    "ratio(ink/fontSize)=${(node.contentBounds?.height ?: -1f) / size}",
            )
        }
    }

    @Test
    fun measureFixedHeightContainerWithText() {
        // Does a text() with a caller-specified height(36dp) container ever get pushed taller
        // by its own text content (line-height overflow)?
        val ui = UiContext()
        ui.beginFrame(400f, 400f, testSnapshot())
        val bounds: UiBounds = ui.createColumn(x = 0f, y = 0f, width = 400f).text(
            "Fixed height row text",
            modifier = Modifier.height(Dimension.Fixed(36f.dp)),
            style = Style { textSize(14f.sp) },
            verticallyCentered = true,
        )
        ui.endFrame()
        println("=== fixed height(36dp) container ===")
        println("returned bounds = $bounds (requested height=36dp)")
        val node = ui.semanticNodes().first { it.label == "Fixed height row text" }
        println("node.bounds=${node.bounds} contentBounds=${node.contentBounds}")
    }
}
