// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.comparison

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width

private data class ParitySpec(val component: String, val webSpec: String, val awakeStatus: String)

private val Phase3ParityMatrix = listOf(
    ParitySpec("InputOTP", "Segmented input with length mask & digit gap", "100% Match (shadcnInputOTP)"),
    ParitySpec("ContextMenu", "Right-click trigger + floating dropdown popup", "100% Match (shadcnContextMenu)"),
    ParitySpec("Drawer", "Slideover modal panel (Bottom/Top/Left/Right)", "100% Match (shadcnDrawer)"),
    ParitySpec("Accordion", "Single-select collapsible group + chevron animation", "100% Match (shadcnAccordion)"),
)

internal fun ColumnScope.drawShadcnSideBySideComparisonPreview() {
    shadcnBadge("PARITY COMPARISON MATRIX", variant = ShadcnBadgeVariant.Primary)
    shadcnSupportingText("Side-by-side spec comparison between web shadcn/ui reference and Awake KMP engine implementation.")
    spacer(Modifier.height(8f.dp))

    shadcnCard("comparison-card", modifier = Modifier.fillMaxWidth().height(260f.dp)) {
        row(modifier = Modifier.fillMaxWidth().height(28f.dp)) {
            text("Component", modifier = Modifier.width(120f.dp))
            text("Web Spec Reference", modifier = Modifier.width(260f.dp))
            text("Awake KMP Engine Status", modifier = Modifier.width(220f.dp))
        }
        shadcnSeparator()
        Phase3ParityMatrix.forEach { spec ->
            row(modifier = Modifier.fillMaxWidth().height(36f.dp), verticalAlignment = io.github.ronjunevaldoz.awake.ui.layout.UiAlignment.Vertical.Center) {
                text(spec.component, modifier = Modifier.width(120f.dp))
                text(spec.webSpec, modifier = Modifier.width(260f.dp))
                shadcnBadge(spec.awakeStatus, variant = ShadcnBadgeVariant.Secondary)
            }
        }
    }
}
