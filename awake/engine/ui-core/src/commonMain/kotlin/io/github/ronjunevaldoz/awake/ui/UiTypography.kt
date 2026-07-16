// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

data class UiTypography(
    val caption: Sp = 12.sp,
    val label: Sp = 14.sp,
    val body: Sp = 16.sp,
    val title: Sp = 20.sp,
    val headline: Sp = 24.sp,
    val display: Sp = 30.sp
) {
    companion object {
        val Default = UiTypography()
    }
}
