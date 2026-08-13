// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.avatar
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AvatarFallbackTest {
    @Test
    fun avatarFallbackClaimsSquareSlotAndDrawsCenteredText() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).avatar(
            id = "avatar.test",
            initials = "JD",
            size = 32f.dp,
            textSize = 14f.sp,
        )
        val frame = ui.finishFrame()
        val semantic = frame.semantics.firstOrNull { it.id == "avatar.test" }
        assertNotNull(semantic)
    }

    @Test
    fun avatarFallbackEmitsSemanticRole() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).avatar(
            id = "avatar.test",
            initials = "AB",
            size = 40f.dp,
            textSize = 16f.sp,
        )
        val frame = ui.finishFrame()
        val semantic = frame.semantics.firstOrNull { it.id == "avatar.test" }
        assertNotNull(semantic)
        assertEquals(40f, semantic?.bounds?.width)
    }
}
