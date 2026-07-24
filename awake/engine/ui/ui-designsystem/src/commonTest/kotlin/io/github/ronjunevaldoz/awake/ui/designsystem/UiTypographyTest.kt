package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingText
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class UiTypographyTest {

    @Test
    fun supportingTextWrapsInsideWrapContentPanels() {
        val ui = UiContext()
        ui.pushFont(UiFonts.bitmap())
        ui.beginFrame(280f, 220f, testSnapshot())

        var panelSlot: UiSlot? = null

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(180f.dp)) {
            surface(id = "copy", modifier = Modifier.copy(heightDimension = Dimension.WrapContent)) { slot ->
                panelSlot = slot
                text("Copy")
                supportingText(
                    "Shared supporting copy should wrap cleanly and grow the panel instead of spilling outside its bounds.",
                    maxLines = 4
                )
            }
        }

        val primitives = ui.endFrame()
        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        val resolvedPanel = assertNotNull(panelSlot)
        assertTrue(
            resolvedPanel.height > 32f,
            "wrap-content panels should grow to fit multi-line supporting copy"
        )
        assertTrue(
            glyphs.any { it.y > resolvedPanel.y + 16f },
            "wrapped supporting copy should render on more than one text row"
        )
    }
}
