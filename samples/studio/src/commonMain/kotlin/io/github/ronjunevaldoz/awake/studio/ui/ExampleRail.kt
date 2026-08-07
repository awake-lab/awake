// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.components.icon
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState

internal fun RowScope.drawExampleRail(
    activeExampleId: String,
    onSelectExample: (String) -> Unit,
) {
    // Local -- nothing outside this panel needs to know about it.
    val expanded = rememberBooleanState(id = "studio-rail", initial = true)

    shadcnSidebar(
        id = "studio-example-rail",
        modifier = Modifier.width(260f.dp).height(Dimension.FillMax),
        expanded = expanded.value,
    ) {
        shadcnSidebarMenu {
            StudioExamples.forEach { example ->
                shadcnSidebarMenuItem(
                    id = "studio-example-${example.id}",
                    label = example.title,
                    active = activeExampleId == example.id,
                    // Expresses intent only; a System performs the actual load.
                    onClick = { onSelectExample(example.id) },
                )
            }
        }
        shadcnButton(
            id = "studio-rail-collapse",
            enabled = true,
            onClick = { expanded.value = !expanded.value },
        ) {
            icon(if (expanded.value) ShadcnIcons.chevronLeft else ShadcnIcons.chevronRight)
        }
    }
}
