// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.shaders

import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression on the exact totals `prepareDrawCalls` (both backends) already depends on --
 * these must never silently drift, since the two backends' uniform-buffer writes are hand-
 * concatenated to match, not generated from this layout. */
class UniformLayoutsTest {
    @Test
    fun texturedUniformLayoutTotalIsSixty() {
        assertEquals(60, TexturedUniformLayout.total)
    }

    @Test
    fun litShadowUniformLayoutTotalIsSixtyEight() {
        assertEquals(68, LitShadowUniformLayout.total)
    }
}
