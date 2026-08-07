// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.avatarFallback
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.testTag
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** [avatarFallback] previously recorded no semantics at all -- both the slot-content primary
 * form and the [String]-initials convenience wrapper must emit an [UiSemanticRole.Avatar] node
 * for the circle itself, on top of whatever the caller's own content draws. */
class AvatarFallbackTest {

    @Test
    fun slotFormRecordsAvatarSemanticAtItsClaimedBounds() {
        val ui = UiContext()
        ui.beginFrame(100f, 100f, testSnapshot())
        ui.createAbsolute(x = 10f, y = 10f).avatarFallback(
            modifier = Modifier.width(32f.px).height(32f.px).testTag("avatar"),
        ) { }

        val semantics = ui.semanticNodes()
        inspectSemanticNodes(semantics).requireClean()
        val node = requireSemanticNode(semantics, id = "avatar", role = UiSemanticRole.Avatar)
        assertEquals(32f, node.bounds.width)
        assertEquals(32f, node.bounds.height)
    }

    @Test
    fun initialsConvenienceStillRecordsTheAvatarRoleAlongsideItsText() {
        val ui = UiContext()
        ui.beginFrame(100f, 100f, testSnapshot())
        ui.pushFont(BitmapFont())
        ui.createAbsolute(x = 10f, y = 10f).avatarFallback(
            "RV",
            modifier = Modifier.width(32f.px).height(32f.px).testTag("avatar"),
        )

        val semantics = ui.semanticNodes()
        inspectSemanticNodes(semantics).requireClean()
        requireSemanticNode(semantics, id = "avatar", role = UiSemanticRole.Avatar)
        assertTrue(
            semantics.any { it.role == UiSemanticRole.Text && it.label == "RV" },
            "the initials text must still render its own semantic node alongside the avatar circle",
        )
    }
}
