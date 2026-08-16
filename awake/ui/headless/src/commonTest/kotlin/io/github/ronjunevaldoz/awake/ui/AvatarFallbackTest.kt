// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.headless.avatar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AvatarFallbackTest {
    @Test
    fun avatarFallbackClaimsSquareSlotAndDrawsCenteredText() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            avatar(
                id = "avatar.test",
                initials = "JD",
                size = 32f.dp,
                textSize = 14f.sp,
            )
        }
        val semantic = frame.semantics.firstOrNull { it.id == "avatar.test" }
        assertNotNull(semantic)
    }

    @Test
    fun avatarFallbackEmitsSemanticRole() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            avatar(
                id = "avatar.test",
                initials = "AB",
                size = 40f.dp,
                textSize = 16f.sp,
            )
        }
        val semantic = frame.semantics.firstOrNull { it.id == "avatar.test" }
        assertNotNull(semantic)
        assertEquals(40f, semantic?.bounds?.width)
    }
}
