// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiAlertDialogResult
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuResult
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.alertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.dialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.dropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.tooltipText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class UiPopupCompositionsTest {

    @Test
    fun tooltipTextUsesPopupPanelAboveAnchor() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(240f, 160f, testSnapshot(x = -100f, y = -100f, down = false))

        var result: UiPopupResult? = null
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(220f.dp)) {
            result = tooltipText(
                anchorSlot = UiSlot(48f, 24f, 96f, 28f),
                visible = true,
                text = "Helpful hint"
            )
        }

        val popupSlot = assertNotNull(assertNotNull(result).slot)
        val primitives = ui.endFrame()
        assertTrue(popupSlot.y >= 52f, "tooltip popup should be placed from the anchor by the popup position provider")
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.Quad })
        assertTrue(primitives.any { it is UiDrawPrimitive.Glyph })
    }

    @Test
    fun dropdownMenuReturnsPickedIndex() {
        val ui = UiContext()
        val anchor = UiSlot(20f, 16f, 120f, 28f)
        var result: UiDropdownMenuResult? = null
        ui.pushFont(BitmapFont())

        ui.beginFrame(220f, 180f, testSnapshot(x = 32f, y = 58f, down = true))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(200f.dp)) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(UiDropdownMenuItem("Open"), UiDropdownMenuItem("Delete", destructive = true)),
                style = Style.Companion { contentPadding(0f.dp) }
            )
        }
        ui.endFrame()
        assertEquals(null, assertNotNull(result).selectedIndex)

        ui.beginFrame(220f, 180f, testSnapshot(x = 32f, y = 58f, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(200f.dp)) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(UiDropdownMenuItem("Open"), UiDropdownMenuItem("Delete", destructive = true)),
                style = Style.Companion { contentPadding(0f.dp) }
            )
        }
        ui.endFrame()

        assertEquals(0, assertNotNull(result).selectedIndex)
        assertFalse(assertNotNull(result).dismissed)
    }

    @Test
    fun dropdownMenuSupportsSeparatorsAndDisabledItems() {
        val ui = UiContext()
        val anchor = UiSlot(20f, 16f, 120f, 28f)
        var result: UiDropdownMenuResult? = null
        ui.pushFont(BitmapFont())

        ui.beginFrame(240f, 220f, testSnapshot(x = 32f, y = 92f, down = true))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(220f.dp)) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem("Pinned", enabled = false),
                    UiDropdownMenuSeparator,
                    UiDropdownMenuItem("Delete", destructive = true, trailingLabel = "Del")
                ),
                style = Style.Companion { contentPadding(0f.dp) }
            )
        }
        ui.endFrame()

        ui.beginFrame(240f, 220f, testSnapshot(x = 32f, y = 92f, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(220f.dp)) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem("Pinned", enabled = false),
                    UiDropdownMenuSeparator,
                    UiDropdownMenuItem("Delete", destructive = true, trailingLabel = "Del")
                ),
                style = Style.Companion { contentPadding(0f.dp) }
            )
        }

        val primitives = ui.endFrame()
        assertEquals(1, assertNotNull(result).selectedIndex, "separators should not consume the selectable index space")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Quad>().isNotEmpty(), "separator should emit a line quad")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size >= 3, "menu entry metadata should still render text glyphs")
    }

    @Test
    fun dialogCentersContentAndDrawsScrim() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        var result: UiPopupResult? = null
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(280f.dp)) {
            result = dialog(
                id = "confirm",
                expanded = true,
                width = Dimension.Fixed(120f.px),
                height = Dimension.Fixed(80f.px),
                header = {
                    text("Confirm")
                }
            ) {
                text("Delete file?")
            }
        }

        val popupSlot = assertNotNull(assertNotNull(result).slot)
        val primitives = ui.endFrame()
        val scrim = primitives.filterIsInstance<UiDrawPrimitive.Quad>().firstOrNull {
            it.x == 0f && it.y == 0f && it.w == 300f && it.h == 200f
        }
        assertNotNull(scrim, "dialog should paint a fullscreen scrim behind the centered popup")
        assertEquals(90f, popupSlot.x)
        assertEquals(60f, popupSlot.y)
        assertFalse(assertNotNull(result).dismissed)
    }

    @Test
    fun alertDialogReturnsConfirmAction() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        var result: UiAlertDialogResult? = null

        ui.beginFrame(320f, 220f, testSnapshot(x = 208f, y = 117f, down = true))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = alertDialog(
                id = "confirm",
                expanded = true,
                title = "Delete",
                message = "Delete this scene?"
            )
        }
        ui.endFrame()

        ui.beginFrame(320f, 220f, testSnapshot(x = 208f, y = 117f, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = alertDialog(
                id = "confirm",
                expanded = true,
                title = "Delete",
                message = "Delete this scene?"
            )
        }
        ui.endFrame()

        assertEquals(UiAlertDialogAction.Confirm, assertNotNull(result).action)
        assertFalse(assertNotNull(result).popup.dismissed)
    }

    @Test
    fun popupMeasurementDoesNotInflateWrapContentLayouts() {
        val ui = UiContext()
        ui.pushFont(UiFonts.bitmap())
        ui.pushTheme(CoreUiTheme)
        val measured = ui.measureColumnContent(width = 220f) { _ ->
            text("Popup proof")
            alertDialog(
                id = "measure-only-dialog",
                expanded = true,
                title = "Delete this scene?",
                message = "The dialog should not affect the parent column height while measuring."
            )
            text("Footer")
        }

        assertTrue(measured.height < 120f, "overlay popups should not poison wrap-content measurement")
        assertTrue(measured.height > 0f, "normal inline content should still contribute to measurement")
    }

    @Test
    fun dialogUsesNeutralDarkScrimByDefault() {
        val ui = UiContext()
        ui.pushFont(UiFonts.bitmap())
        ui.beginFrame(320f, 240f,  testSnapshot())

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            dialog(
                id = "dialog",
                expanded = true
            ) {
                text("Dialog body")
            }
        }

        val scrim = ui.endFrame().filterIsInstance<UiDrawPrimitive.Quad>().firstOrNull()
        assertEquals(
            Color.Black.withAlpha(0.48f),
            scrim?.color,
            "dialogs should default to a neutral dark scrim so light themes do not wash the scene out"
        )
    }

    @Test
    fun dialogActionButtonLabelInheritsThemedForeground() {
        // Regression test for the "button label not displayed" bug: a dialog action button's
        // Slot-API content lambda (`shadcnButton(id, ...) { text(...) }`) must inherit the
        // button's resolved themed foreground as its ambient text color, the same way
        // surface()/row()/column()/box() propagate their resolved text style to children.
        // Without that propagation, `text()` inside the lambda falls back to whatever ambient
        // color was active outside the dialog/button (e.g. the page's default foreground),
        // which reads as "not displayed" against a differently-colored button background.
        val ui = UiContext()
        val theme = ShadcnTheme
        ui.pushFont(BitmapFont())
        ui.pushTheme(theme)
        ui.pushTextStyle(TextStyle(color = theme.tokens.foreground))
        ui.beginFrame(320f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            dialog(id = "confirm", expanded = true, actions = {
                shadcnButton(
                    id = "confirm.action",
                    variant = ShadcnButtonVariant.Primary,
                    modifier = Modifier.width(88f.dp)
                ) {
                    text("Confirm")
                }
            }) { /* empty body -- isolates the glyphs below to the action button's label */ }
        }

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "dialog action button label should render glyphs")
        val primaryForeground = theme.tokens.primaryForeground
        glyphs.forEach { glyph ->
            assertEquals(
                primaryForeground,
                glyph.color,
                "dialog action button's slot content should inherit the button's resolved themed foreground, not the ambient page color"
            )
        }
    }
}
