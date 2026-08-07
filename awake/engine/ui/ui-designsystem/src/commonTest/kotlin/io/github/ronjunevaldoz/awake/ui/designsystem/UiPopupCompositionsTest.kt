// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiAlertDialogResult
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuResult
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnAlertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.style.*
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiPopupCompositionsTest {

    @Test
    fun tooltipTextUsesPopupPanelAboveAnchor() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(240f, 160f, testSnapshot(x = -100f, y = -100f, down = false))

        var result: UiPopupResult? = null
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(220f.dp)) {
            result = shadcnTooltipText(
                anchorSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(48f, 24f, 96f, 28f),
                visible = true,
                text = "Helpful hint",
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
        val anchor = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 16f, 120f, 28f)
        var result: UiDropdownMenuResult? = null
        ui.pushFont(BitmapFont())

        ui.beginFrame(220f, 180f, testSnapshot(x = 32f, y = 58f, down = true))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(200f.dp)) {
            result = shadcnDropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(UiDropdownMenuItem("Open"), UiDropdownMenuItem("Delete", destructive = true)),
                style = Style.Companion { contentPadding(0f.dp) },
            )
        }
        ui.endFrame()
        assertEquals(null, assertNotNull(result).selectedIndex)

        ui.beginFrame(220f, 180f, testSnapshot(x = 32f, y = 58f, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(200f.dp)) {
            result = shadcnDropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(UiDropdownMenuItem("Open"), UiDropdownMenuItem("Delete", destructive = true)),
                style = Style.Companion { contentPadding(0f.dp) },
            )
        }
        ui.endFrame()

        assertEquals(0, assertNotNull(result).selectedIndex)
        assertFalse(assertNotNull(result).dismissed)
    }

    @Test
    fun dropdownMenuSupportsSeparatorsAndDisabledItems() {
        val ui = UiContext()
        val anchor = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 16f, 120f, 28f)
        var result: UiDropdownMenuResult? = null
        ui.pushFont(BitmapFont())

        ui.beginFrame(240f, 220f, testSnapshot(x = 32f, y = 92f, down = true))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(220f.dp)) {
            result = shadcnDropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem("Pinned", enabled = false),
                    UiDropdownMenuSeparator,
                    UiDropdownMenuItem("Delete", destructive = true, trailingLabel = "Del"),
                ),
                style = Style.Companion { contentPadding(0f.dp) },
            )
        }
        ui.endFrame()

        ui.beginFrame(240f, 220f, testSnapshot(x = 32f, y = 92f, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(220f.dp)) {
            result = shadcnDropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem("Pinned", enabled = false),
                    UiDropdownMenuSeparator,
                    UiDropdownMenuItem("Delete", destructive = true, trailingLabel = "Del"),
                ),
                style = Style.Companion { contentPadding(0f.dp) },
            )
        }

        val primitives = ui.endFrame()
        assertEquals(1, assertNotNull(result).selectedIndex, "separators should not consume the selectable index space")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Quad>().isNotEmpty(), "separator should emit a line quad")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size >= 3, "menu entry metadata should still render text glyphs")
    }

    @Test
    fun dropdownMenuWithConstrainedHeightScrollsInsteadOfClippingSilently() {
        // Regression test for the reported "dropdown menu items not scrollable" bug: a menu
        // with more items than fit its (caller-constrained) height used to clip the overflow
        // with no way to reach it -- shadcnDropdownMenu's surface never wired a scrollState.
        val ui = UiContext()
        val anchor = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 16f, 160f, 28f)
        ui.pushFont(BitmapFont())
        ui.beginFrame(320f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        val items = (1..10).map { UiDropdownMenuItem("Option $it") }
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            shadcnDropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = items,
                // 10 items * 32px = 320px of content, well beyond this 96px-tall menu.
                height = Dimension.Fixed(96f.px),
                style = Style.Companion { contentPadding(0f.dp) },
            )
        }
        val frame = ui.finishFrame()

        assertTrue(
            frame.primitives.filterIsInstance<UiDrawPrimitive.ClipPush>().isNotEmpty(),
            "a height-constrained menu with overflowing items must clip its viewport",
        )
        // The menu surface itself must stay pinned to the constrained height, not grow to fit
        // all 10 items -- proof the container is actually height-bounded, not silently
        // expanding (the pre-fix WrapContent-shaped clip-without-scroll symptom).
        val menuSemanticsBounds = assertNotNull(frame.semantics.firstOrNull { it.id == "menu.menu" }).bounds
        assertEquals(96f, menuSemanticsBounds.height, "a Fixed-height menu must not grow to fit overflowing items")
        // A scrollbar thumb (a small quad/rounded-quad near the menu's right edge) must be
        // present -- the actual affordance that makes the rest of the list reachable.
        val menuBounds = assertNotNull(frame.semantics.firstOrNull { it.id == "menu.menu" }).bounds
        val hasScrollThumb = frame.primitives.any { primitive ->
            val (x, w) = when (primitive) {
                is UiDrawPrimitive.Quad -> primitive.x to primitive.w
                is UiDrawPrimitive.RoundedQuad -> primitive.x to primitive.w
                else -> return@any false
            }
            w <= 8f && x >= menuBounds.x + menuBounds.width - 12f
        }
        assertTrue(hasScrollThumb, "a height-constrained overflowing menu must render a scroll thumb")
    }

    @Test
    fun dialogCentersContentAndDrawsScrim() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        var result: UiPopupResult? = null
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(280f.dp)) {
            result = shadcnDialog(
                id = "confirm",
                expanded = true,
                width = Dimension.Fixed(120f.px),
                height = Dimension.Fixed(80f.px),
                header = {
                    text("Confirm")
                },
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

        // First frame: no pointer down yet, just render to find the Confirm button's real
        // position -- clicking a hardcoded pixel guess breaks the moment the actions row's
        // arrangement changes (e.g. Arrangement.SpaceBetween moving Confirm to the row's right
        // edge instead of wherever it used to sit).
        ui.beginFrame(320f, 220f, testSnapshot(x = -1f, y = -1f, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = shadcnAlertDialog(
                id = "confirm",
                expanded = true,
                title = "Delete",
                message = "Delete this scene?",
            )
        }
        val confirmBounds = assertNotNull(
            ui.finishFrame().semantics.firstOrNull { it.id == "confirm.confirm" },
        ).bounds
        val clickX = confirmBounds.x + confirmBounds.width / 2f
        val clickY = confirmBounds.y + confirmBounds.height / 2f

        ui.beginFrame(320f, 220f, testSnapshot(x = clickX, y = clickY, down = true))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = shadcnAlertDialog(
                id = "confirm",
                expanded = true,
                title = "Delete",
                message = "Delete this scene?",
            )
        }
        ui.endFrame()

        ui.beginFrame(320f, 220f, testSnapshot(x = clickX, y = clickY, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = shadcnAlertDialog(
                id = "confirm",
                expanded = true,
                title = "Delete",
                message = "Delete this scene?",
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
        ui.pushTheme(UiDefaultTheme)
        val measured = ui.measureColumnContent(width = 220f) { _ ->
            text("Popup proof")
            shadcnAlertDialog(
                id = "measure-only-dialog",
                expanded = true,
                title = "Delete this scene?",
                message = "The dialog should not affect the parent column height while measuring.",
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
        ui.beginFrame(320f, 240f, testSnapshot())

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            shadcnDialog(
                id = "dialog",
                expanded = true,
            ) {
                text("Dialog body")
            }
        }

        val scrim = ui.endFrame().filterIsInstance<UiDrawPrimitive.Quad>().firstOrNull()
        assertEquals(
            Color.Black.withAlpha(0.48f),
            scrim?.color,
            "dialogs should default to a neutral dark scrim so light themes do not wash the scene out",
        )
    }

    @Test
    fun dropdownMenuItemLabelDoesNotOverlapSupportingTextOrTrailingLabel() {
        // Regression test for the item label/supporting-text overlap bug: a two-line item
        // (label + supporting text) grows its box taller to fit the stack, but the label used
        // to center vertically in that taller box regardless, drifting down into the supporting
        // text's fixed top-offset position. The trailing shortcut had a matching horizontal bug
        // (overflow=Ellipsis silently widened its claimed box to FillMax, leaving align(End)
        // nothing to shift into, so it drew left-anchored under the label).
        val ui = UiContext()
        val anchor = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 16f, 120f, 28f)
        ui.pushFont(BitmapFont())
        ui.beginFrame(320f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            shadcnDropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem(
                        label = "Duplicate panel",
                        trailingLabel = "Cmd+D",
                        shadcnSupportingText = "Secondary action metadata sits on the trailing edge.",
                    ),
                ),
                style = Style.Companion { contentPadding(0f.dp) },
            )
        }

        val semantics = ui.finishFrame().semantics
        val label = assertNotNull(semantics.firstOrNull { it.label == "Duplicate panel" })
        val supporting = assertNotNull(
            semantics.firstOrNull { it.label == "Secondary action metadata sits on the trailing edge." },
        )
        val trailing = assertNotNull(semantics.firstOrNull { it.label == "Cmd+D" })

        assertTrue(
            label.bounds.y + label.bounds.height <= supporting.bounds.y + 1f,
            "item label must sit above its own supporting text, not overlap it",
        )
        assertTrue(
            trailing.bounds.x >= label.bounds.x + label.bounds.width,
            "trailing shortcut must sit to the right of the label, not overlap it",
        )
    }

    @Test
    fun dropdownMenuItemIconRendersWithoutShiftingIconLessItems() {
        // Proof test for the optional leading-icon slot on UiDropdownMenuItem: an icon-bearing
        // item reserves space for its icon and shifts its label right to make room, while an
        // icon-less item in the same menu keeps the original 12dp label start untouched.
        val ui = UiContext()
        val anchor = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 16f, 120f, 28f)
        ui.pushFont(BitmapFont())
        ui.beginFrame(320f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            shadcnDropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem(
                        label = "With icon",
                        icon = { text("*") },
                    ),
                    UiDropdownMenuItem(label = "No icon"),
                ),
                style = Style.Companion { contentPadding(0f.dp) },
            )
        }

        val semantics = ui.finishFrame().semantics
        val iconLabel = assertNotNull(semantics.firstOrNull { it.label == "With icon" })
        val icon = assertNotNull(semantics.firstOrNull { it.label == "*" })
        val plainLabel = assertNotNull(semantics.firstOrNull { it.label == "No icon" })

        assertTrue(
            icon.bounds.x + icon.bounds.width <= iconLabel.bounds.x + 1f,
            "leading icon must sit to the left of its own item's label, not overlap it",
        )
        assertEquals(
            anchor.x + 8f,
            plainLabel.bounds.x,
            "an icon-less item's label start must match shadcn px-2 horizontal padding (8dp)",
        )
        assertTrue(
            iconLabel.bounds.x > plainLabel.bounds.x,
            "an icon-bearing item's label must shift right to make room for its icon",
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
        ui.pushTextStyle(TextStyle(color = theme.colors.foreground))
        ui.beginFrame(320f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            shadcnDialog(id = "confirm", expanded = true, actions = {
                shadcnButton(
                    id = "confirm.action",
                    variant = ShadcnButtonVariant.Primary,
                    modifier = Modifier.width(88f.dp),
                ) {
                    text("Confirm")
                }
            }) { /* empty body -- isolates the glyphs below to the action button's label */ }
        }

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "dialog action button label should render glyphs")
        val primaryForeground = theme.colors.primaryForeground
        glyphs.forEach { glyph ->
            assertEquals(
                primaryForeground,
                glyph.color,
                "dialog action button's slot content should inherit the button's resolved themed foreground, not the ambient page color",
            )
        }
    }

    // Regression coverage for the audit-driven shadcnDialog close-button option and the
    // shadcnAlertDialog actions-slot overload (the 3-choice Save/Discard/Cancel case the
    // fixed-2-button API made impossible).

    @Test
    fun dialogCloseButtonRendersAndFiresDismiss() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())

        ui.beginFrame(320f, 220f, testSnapshot(x = -1f, y = -1f, down = false))
        var result: UiPopupResult? = null
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = shadcnDialog(
                id = "closable",
                expanded = true,
                width = Dimension.Fixed(200f.px),
                height = Dimension.Fixed(120f.px),
                showCloseButton = true,
            ) {
                text("Dialog with a close affordance")
            }
        }
        val closeBounds = assertNotNull(
            ui.finishFrame().semantics.firstOrNull { it.id == "closable.close" },
        ).bounds
        val clickX = closeBounds.x + closeBounds.width / 2f
        val clickY = closeBounds.y + closeBounds.height / 2f

        ui.beginFrame(320f, 220f, testSnapshot(x = clickX, y = clickY, down = true))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = shadcnDialog(
                id = "closable",
                expanded = true,
                width = Dimension.Fixed(200f.px),
                height = Dimension.Fixed(120f.px),
                showCloseButton = true,
            ) {
                text("Dialog with a close affordance")
            }
        }
        ui.endFrame()

        ui.beginFrame(320f, 220f, testSnapshot(x = clickX, y = clickY, down = false))
        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            result = shadcnDialog(
                id = "closable",
                expanded = true,
                width = Dimension.Fixed(200f.px),
                height = Dimension.Fixed(120f.px),
                showCloseButton = true,
            ) {
                text("Dialog with a close affordance")
            }
        }
        ui.endFrame()

        assertTrue(assertNotNull(result).dismissed, "clicking the reserved close button should report the dialog as dismissed")
    }

    @Test
    fun alertDialogActionsSlotAllowsThreeChoices() {
        // The audit's exact "impossible today" case: Save/Discard/Cancel, not just confirm/dismiss.
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(320f, 220f, testSnapshot(x = -1f, y = -1f, down = false))

        ui.column(modifier = Modifier.offset(0f.dp, 0f.dp).width(300f.dp)) {
            shadcnAlertDialog(
                id = "unsaved",
                expanded = true,
                title = "Unsaved changes",
                actions = {
                    shadcnButton(id = "unsaved.save", label = "Save", modifier = Modifier.width(80f.dp))
                    shadcnButton(id = "unsaved.discard", label = "Discard", modifier = Modifier.width(80f.dp))
                    shadcnButton(id = "unsaved.cancel", label = "Cancel", modifier = Modifier.width(80f.dp))
                },
            ) {
                text("You have unsaved changes.")
            }
        }

        val semantics = ui.finishFrame().semantics
        assertNotNull(semantics.firstOrNull { it.id == "unsaved.save" }, "Save action should render")
        assertNotNull(semantics.firstOrNull { it.id == "unsaved.discard" }, "Discard action should render")
        assertNotNull(semantics.firstOrNull { it.id == "unsaved.cancel" }, "Cancel action should render")
    }
}
