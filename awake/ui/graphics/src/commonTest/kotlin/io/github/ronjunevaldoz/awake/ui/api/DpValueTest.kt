// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api

import kotlin.test.Test
import kotlin.test.assertEquals

class DpValueTest {
    @Test
    fun literalBuildersAndArithmeticRemainRuntimeFreeValues() {
        assertEquals(Dp(12f), 12.dp)
        assertEquals(Sp(14f), 14.sp)
        assertEquals(Dp(10f), 12.dp - 2.dp)
        assertEquals(Dp(24f), 12.dp * 2f)
    }
}
