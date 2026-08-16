// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnAvatarSize
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatarBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatarGroup
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.box
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Covers `ShadcnAvatarSize`, `shadcnAvatarBadge`, and `shadcnAvatarGroup` (Fix 1 in the
 * shadcn-compose parity audit). One file per behavior set per docs/reference/ui-ownership.md's
 * Test File Rule. */
class ShadcnAvatarTest {

    @Test
    fun avatarSizeDrivesBoxDiameterWhenModifierOmitsOne() {
        val frame = renderShadcnComponent(width = 200f, height = 200f) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnAvatar(id = "avatar", initials = "A", size = ShadcnAvatarSize.Sm)
            }
        }
        val circle = frame.primitivesOf<UiDrawPrimitive.RoundedQuad>().first()
        assertTrue(abs(ShadcnAvatarSize.Sm.boxSize.value - circle.w) <= 0.5f)
    }

    @Test
    fun avatarSizeScalesInitialsTextTallerForLg() {
        fun glyphHeight(size: ShadcnAvatarSize): Float = renderShadcnComponent(
            width = 200f,
            height = 200f,
        ) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnAvatar(id = "avatar", initials = "A", size = size)
            }
        }.primitivesOf<UiDrawPrimitive.Glyph>().first().h

        assertTrue(
            glyphHeight(ShadcnAvatarSize.Lg) > glyphHeight(ShadcnAvatarSize.Sm),
            "Lg avatar initials should render taller than Sm avatar initials",
        )
    }

    /**
     * The avatar is ONE circle with a letter in it. The label used to be handed the container's
     * own style -- background and full corner radius included -- so it painted a second filled
     * circle behind the initials, inside the avatar's circle.
     */
    @Test
    fun avatarDrawsOneCircleForItsOwnFillAndNoneBehindTheInitials() {
        val frame = renderShadcnComponent(width = 200f, height = 200f) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnAvatar(id = "avatar", initials = "A", size = ShadcnAvatarSize.Default)
            }
        }
        val circles = frame.primitivesOf<UiDrawPrimitive.RoundedQuad>()
        assertEquals(
            1,
            circles.size,
            "a plain avatar draws exactly its own circle, got ${circles.map { "${it.w}x${it.h}" }}",
        )
    }

    @Test
    fun avatarBadgeDrawsASecondSmallerCircleAtTheCorner() {
        val size = ShadcnAvatarSize.Default
        val frame = renderShadcnComponent(width = 200f, height = 200f) {
            box(modifier = Modifier.fillMaxSize()) {
                column(modifier = Modifier.width(size.boxSize).height(size.boxSize)) {
                    shadcnAvatar(id = "avatar", initials = "A", size = size)
                }
                shadcnAvatarBadge(
                    modifier = Modifier.offset(
                        x = size.boxSize - size.badgeSize,
                        y = size.boxSize - size.badgeSize,
                    ),
                )
            }
        }

        val circles = frame.primitivesOf<UiDrawPrimitive.RoundedQuad>()
        assertTrue(circles.size >= 2, "expected an avatar circle and a badge circle, got ${circles.size}")
        val avatarCircle = circles.maxByOrNull { it.w }!!
        val badgeCircle = circles.filter { it !== avatarCircle }.maxByOrNull { it.w }!!
        assertTrue(abs(size.badgeSize.value - badgeCircle.w) <= 0.5f)
        // Badge sits at the avatar's bottom-end corner, not centered inside it.
        assertTrue(badgeCircle.x + badgeCircle.w > avatarCircle.x + avatarCircle.w - badgeCircle.w - 1f)
        assertTrue(badgeCircle.y + badgeCircle.h > avatarCircle.y + avatarCircle.h - badgeCircle.h - 1f)
    }

    /**
     * Asserts on the avatar boxes, not on their initials: the labels are centred inside their
     * own box, so consecutive labels never overlap even when the boxes do -- an overlap
     * assertion on the text passes or fails for reasons that have nothing to do with grouping.
     * Without the offset the boxes would abut exactly (0/32/64), so this still fails if the
     * overlap is removed.
     */
    @Test
    fun avatarGroupOverlapsAvatarsAfterTheFirst() {
        val frame = renderShadcnComponent(width = 300f, height = 200f) {
            row(modifier = Modifier.fillMaxSize()) {
                shadcnAvatarGroup(initials = listOf("A", "B", "C"))
            }
        }
        val first = frame.bounds("avatar.0")
        val second = frame.bounds("avatar.1")
        val third = frame.bounds("avatar.2")

        assertTrue(second.x < first.x + first.width, "second avatar should overlap the first")
        assertTrue(third.x < second.x + second.width, "third avatar should overlap the second")
    }
}
