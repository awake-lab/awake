// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont

/**
 * Drives the REAL "text-input" showcase page -- the actual [ShowcasePages] entry,
 * [renderUiShowcasePagePreview] dispatch, [drawUiShowcaseTextInputPreview] composition, and
 * [io.github.ronjunevaldoz.awake.ui.designsystem.shadcnFieldTextField] wrapper -- across
 * multiple frames with a persisted [UiContext], the same shape [io.github.ronjunevaldoz.awake
 * .ui.GameUiRuntime.render] uses in the live app. Every prior text-field test exercised
 * `textField()` in isolation with a hand-built [UiContext]; none of them went through this
 * page's actual composition path, which is exactly where a live-reported "keys aren't consumed,
 * typed text isn't visible" bug could hide even with the widget-level tests green.
 */
class UiShowcaseTextInputIntegrationTest {

    @Test
    fun clickingTheRealPageFieldGrantsFocusAndRendersTypedGlyphs() {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val font = UiFonts.default()
        val page = ShowcasePages.first { it.id == "text-input" }
        val ui = UiContext()
        val input = Input()
        val width = 900f
        val height = 460f

        fun frame(pointerDown: Boolean, x: Float, y: Float): List<UiDrawPrimitive> {
            input.setPointer(down = pointerDown, x = x, y = y)
            ui.beginFrame(UiFrameInput(viewportWidth = width, viewportHeight = height, input = input.updateSnapshot().toUiInputState()))
            ui.pushLocal(LocalFont, font)
            ui.showcaseRoot(theme = theme) {
                column(
                    modifier = Modifier.offset(24f.dp, 24f.dp)
                        .width((width - 48f).dp)
                        .height((height - 48f).dp),
                    verticalArrangement = Arrangement.spacedBy(10f.dp),
                ) {
                    renderUiShowcasePagePreview(page, state)
                }
            }
            return ui.finishFrame().primitives
        }

        // Frame 1: locate the real field via its recorded semantics, same as an E2E test
        // would locate an element -- not a hand-guessed pixel coordinate.
        frame(pointerDown = false, x = -100f, y = -100f)
        val nameField = ui.finishFrame().semantics
            .firstOrNull { it.role == UiSemanticRole.Text && it.id == "showcase-name" }
        requireNotNull(nameField) { "showcase-name text field must be present in the real page's semantics" }
        val clickX = nameField.bounds.x + nameField.bounds.width / 2f
        val clickY = nameField.bounds.y + nameField.bounds.height / 2f

        // Frame 2: click into the field (matches the desktop bridge's ordering -- pollInput
        // sets pointer state, then the UI frame runs).
        frame(pointerDown = true, x = clickX, y = clickY)
        assertTrue(
            ui.isFocusedInternal("showcase-name"),
            "clicking the real page's field must grant it focus",
        )

        // Frame 3: type, exactly as the desktop bridge does -- push BEFORE beginFrame.
        input.pushTypedText("Hi")
        val primitives = frame(pointerDown = false, x = clickX, y = clickY)

        val glyphCount = primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size
        assertTrue(
            glyphCount > 0,
            "typed text must actually render as glyph primitives, not just update hidden state",
        )

        val nameLabel = ui.finishFrame().semantics
            .firstOrNull { it.role == UiSemanticRole.Text && it.id == "showcase-name" }?.label
        assertEquals(
            "Hi",
            nameLabel,
            "the field's value must reflect what was typed after going through the real page composition",
        )
    }

    @Test
    fun typedTextIsIgnoredBeforeAnyFieldIsClicked() {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val font = UiFonts.default()
        val page = ShowcasePages.first { it.id == "text-input" }
        val ui = UiContext()
        val input = Input()

        input.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(UiFrameInput(viewportWidth = 900f, viewportHeight = 460f, input = input.updateSnapshot().toUiInputState()))
        ui.pushLocal(LocalFont, font)
        ui.showcaseRoot(theme = theme) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(852f.dp).height(412f.dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                renderUiShowcasePagePreview(page, state)
            }
        }
        ui.finishFrame().primitives

        input.pushTypedText("ignored")
        input.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(UiFrameInput(viewportWidth = 900f, viewportHeight = 460f, input = input.updateSnapshot().toUiInputState()))
        ui.pushLocal(LocalFont, font)
        ui.showcaseRoot(theme = theme) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(852f.dp).height(412f.dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                renderUiShowcasePagePreview(page, state)
            }
        }
        ui.finishFrame().primitives

        val label = ui.finishFrame().semantics
            .firstOrNull { it.role == UiSemanticRole.Text && it.id == "showcase-name" }?.label
        assertEquals(
            "Jane Doe",
            label,
            "typed text must not leak into a field that was never clicked -- value stays empty, so its placeholder is still what's recorded",
        )
    }
}
