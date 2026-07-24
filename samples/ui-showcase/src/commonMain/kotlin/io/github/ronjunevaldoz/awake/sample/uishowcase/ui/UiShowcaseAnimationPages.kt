// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.shadcnShimmer
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun ColumnScope.drawUiShowcaseShimmerPreview() {
    shadcnSectionHeader(
        title = "Shimmer Effect",
        description = "A subtle sweeping highlight applied to text and components."
    )
    spacer(Modifier.height(16f.dp))
    
    shadcnText(
        label = "Generating response...",
        modifier = Modifier.shadcnShimmer()
    )
    
    spacer(Modifier.height(12f.dp))
    
    shadcnText(
        label = "LOADING SCENE ASSETS",
        style = Style { textSize(14f.sp) },
        modifier = Modifier.shadcnShimmer()
    )
}
