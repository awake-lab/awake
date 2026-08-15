// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.weight
import io.github.ronjunevaldoz.awake.ui.headless.width

// Sample-local, not part of the shared shadcn icon vocabulary -- HeroIcons directly, same
// reasoning IconRail.kt documents for its own tool glyphs. rotating-cube uses ShadcnIcons.cube
// specifically, since that glyph is already registered there.
/**
 * Docked "Scene Browser" panel -- flush to the window edge, no card border/radius of its own;
 * the resizable group's own handle line (see `drawStudioShellBody`) is the seam against the
 * viewport, and [shadcnSidebar]'s `header` slot supplies the one hairline below the title row.
 *
 * The list's own collapse toggle now lives in that header as a small ghost icon button, not the
 * stray full-width button the old under-list toggle rendered as (no `variant` meant it defaulted
 * to `ShadcnButtonVariant.Primary`, which in light theme resolves to a near-black fill spanning
 * the sidebar's full width -- exactly the "solid black bar" reported). It only hides the list
 * content now -- panel width is the resizable group's own job, not this toggle's.
 */
internal fun UiScope.drawExampleRail(
    activeExampleId: String,
    onSelectExample: (String) -> Unit,
) {
    val expanded = rememberBooleanState(id = "studio-example-rail-expanded", initial = true)
    shadcnSidebar(
        id = "studio-example-rail",
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        header = {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                shadcnButton(
                    id = "studio-example-rail-toggle",
                    label = if (expanded.value) "⌄" else "›",
                    modifier = Modifier.width(ShadcnButtonSize.Icon.heightDp),
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Icon,
                    onClick = { expanded.value = !expanded.value },
                )
                shadcnText("Scene Browser", modifier = Modifier.weight(1f))
            }
        },
    ) {
        if (expanded.value) {
            shadcnSidebarMenu {
                StudioExamples.forEach { example ->
                    val active = activeExampleId == example.id
                    exampleMenuItem(
                        id = "studio-example-${example.id}",
                        label = example.title,
                        active = active,
                        onClick = { onSelectExample(example.id) },
                    )
                }
            }
        }
    }
}

private fun io.github.ronjunevaldoz.awake.ui.headless.ColumnScope.exampleMenuItem(
    id: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    shadcnSidebarMenuItem(
        id = id,
        label = label,
        active = active,
        onClick = onClick,
    )
}
