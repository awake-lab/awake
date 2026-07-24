// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.column
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiShowcaseTextareaIntegrationTest {

    @Test
    fun clickingTheRealPageTextareaGrantsFocusAndRendersTypedGlyphs() {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val font = UiFonts.default()
        val page = ShowcasePages.first { it.id == "text-input" }
        val ui = UiContext()
        val input = Input()
        val width = 900f
        val height = 600f

        fun frame(pointerDown: Boolean, x: Float, y: Float): List<UiDrawPrimitive> {
            input.setPointer(down = pointerDown, x = x, y = y)
            ui.beginFrame(width, height, input.updateSnapshot().toUiInputState())
            ui.pushFont(font)
            ui.pushTheme(theme)
            ui.column(
                modifier = Modifier
                    .offset(24f.dp, 24f.dp)
                    .width((width - 48f).dp)
                    .height((height - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp)
            ) {
                renderUiShowcasePagePreview(page, state)
            }
            return ui.endFrame()
        }

        // Frame 1: locate the textarea via its recorded semantics
        frame(pointerDown = false, x = -100f, y = -100f)
        val bioField = ui.semanticNodes()
            .firstOrNull { it.role == UiSemanticRole.Text && it.id == "showcase-bio" }
        requireNotNull(bioField) { "showcase-bio textarea must be present in the real page's semantics" }
        val clickX = bioField.bounds.x + bioField.bounds.width / 2f
        val clickY = bioField.bounds.y + bioField.bounds.height / 2f

        // Frame 2: click into the field
        frame(pointerDown = true, x = clickX, y = clickY)
        assertTrue(
            ui.isFocused("showcase-bio"),
            "clicking the real page's textarea must grant it focus"
        )

        // Frame 3: type
        input.pushTypedText("Hello World\nLine 2")
        val primitives = frame(pointerDown = false, x = clickX, y = clickY)

        val glyphCount = primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size
        assertTrue(glyphCount > 0, "typed text must actually render as glyph primitives")

        val bioLabel = ui.semanticNodes()
            .firstOrNull { it.role == UiSemanticRole.Text && it.id == "showcase-bio" }?.label
        assertEquals(
            "Hello World\nLine 2",
            bioLabel,
            "the textarea's value must reflect what was typed"
        )
    }

    @Test
    fun textareaSupportsAutomaticWrapping() {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val font = UiFonts.default()
        val page = ShowcasePages.first { it.id == "text-input" }
        val ui = UiContext()
        val input = Input()

        // Use a very narrow width to force wrapping
        val width = 160f
        val height = 600f

        fun frame(value: String): List<UiDrawPrimitive> {
            input.pushTypedText(value)
            ui.beginFrame(width, height, input.updateSnapshot().toUiInputState())
            ui.pushFont(font)
            ui.pushTheme(theme)
            ui.column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width((width - 48f).dp)
                    .height((height - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp)
            ) {
                renderUiShowcasePagePreview(page, state)
            }
            return ui.endFrame()
        }

        // Frame 1: Type a very long word to ensure it wraps even without spaces
        val longWord = "ANTIDISESTABLISHMENTARIANISM_ANTIDISESTABLISHMENTARIANISM"
        frame(longWord)

        val bioNode = ui.semanticNodes()
            .firstOrNull { it.role == UiSemanticRole.Text && it.id == "showcase-bio" }
        requireNotNull(bioNode)

        // Check line count in semantics
        // Note: recordSemantic for Text nodes now includes lineCount
        val lineCount = bioNode.lineCount
        assertTrue(
            lineCount > 1,
            "Long text without manual newlines must wrap into multiple lines (got $lineCount)"
        )
    }
}
