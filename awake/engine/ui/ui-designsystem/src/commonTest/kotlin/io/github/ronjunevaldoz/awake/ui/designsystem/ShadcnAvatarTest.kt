// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnAvatarSize
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatarBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatarGroup
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Covers `ShadcnAvatarSize`, `shadcnAvatarBadge`, and `shadcnAvatarGroup` (Fix 1 in the
 * shadcn-compose parity audit). One file per behavior set per docs/reference/ui-ownership.md's
 * Test File Rule. */
class ShadcnAvatarTest {

    @Test
    fun avatarSizeDrivesBoxDiameterWhenModifierOmitsOne() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(200f, 200f, testSnapshot())

        ui.createColumn(slot = ui.frameBounds()).shadcnAvatar("A", size = ShadcnAvatarSize.Sm)

        val output = ui.finishFrame()
        val circle = output.primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().first()
        assertTrue(abs(ShadcnAvatarSize.Sm.boxSize.value - circle.w) <= 0.5f)
    }

    @Test
    fun avatarSizeScalesInitialsTextTallerForLg() {
        fun glyphHeight(size: ShadcnAvatarSize): Float {
            val ui = UiContext()
            ui.pushFont(BitmapFont())
            ui.pushTheme(ShadcnTheme)
            ui.beginFrame(200f, 200f, testSnapshot())
            ui.createColumn(slot = ui.frameBounds()).shadcnAvatar("A", size = size)
            return ui.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Glyph>().first().h
        }

        assertTrue(
            glyphHeight(ShadcnAvatarSize.Lg) > glyphHeight(ShadcnAvatarSize.Sm),
            "Lg avatar initials should render taller than Sm avatar initials",
        )
    }

    @Test
    fun avatarBadgeDrawsASecondSmallerCircleAtTheCorner() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(200f, 200f, testSnapshot())

        ui.createColumn(slot = ui.frameBounds()).shadcnAvatar(modifier = Modifier, size = ShadcnAvatarSize.Default) {
            shadcnAvatarBadge()
        }

        // The badge's own border ring draws as an outer + inner RoundedQuad pair (see
        // emitFillAndBorder's bordered-circle path), on top of the avatar's own single
        // (borderless) fill circle -- at least one badge-sized circle plus the avatar's
        // larger circle should be present, not just the avatar alone.
        val circles = ui.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertTrue(circles.size >= 2, "expected at least an avatar circle and a badge circle, got ${circles.size}")
        val avatarCircle = circles.maxByOrNull { it.w }!!
        val badgeCircle = circles.filter { it !== avatarCircle }.maxByOrNull { it.w }!!
        assertTrue(abs(ShadcnAvatarSize.Default.badgeSize.value - badgeCircle.w) <= 0.5f)
        // Badge should sit at the avatar's bottom-end corner, not centered inside it.
        assertTrue(badgeCircle.x + badgeCircle.w > avatarCircle.x + avatarCircle.w - badgeCircle.w - 1f)
        assertTrue(badgeCircle.y + badgeCircle.h > avatarCircle.y + avatarCircle.h - badgeCircle.h - 1f)
    }

    @Test
    fun avatarGroupOverlapsAvatarsAfterTheFirst() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 200f, testSnapshot())

        ui.createRow(slot = ui.frameBounds()).shadcnAvatarGroup(initials = listOf("A", "B", "C"))

        val semantics = ui.finishFrame().semantics
        val a = assertNotNull(semantics.firstOrNull { it.label == "A" })
        val b = assertNotNull(semantics.firstOrNull { it.label == "B" })
        val c = assertNotNull(semantics.firstOrNull { it.label == "C" })

        // Each avatar after the first is shifted left by `overlap`, so it visually overlaps
        // (not just abuts) its predecessor's box.
        assertTrue(b.bounds.x < a.bounds.x + a.bounds.width, "second avatar should overlap the first")
        assertTrue(c.bounds.x < b.bounds.x + b.bounds.width, "third avatar should overlap the second")
    }
}
