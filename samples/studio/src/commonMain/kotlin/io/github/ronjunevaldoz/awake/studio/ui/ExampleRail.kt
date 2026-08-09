// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon

// Sample-local, not part of the shared shadcn icon vocabulary -- HeroIcons directly, same
// reasoning IconRail.kt documents for its own tool glyphs. rotating-cube uses ShadcnIcons.cube
// specifically, since that glyph is already registered there.
private val ExampleIcons: Map<String, UiImageVector> = mapOf(
    "rotating-cube" to ShadcnIcons.cube,
    "gltf-viewer" to HeroIcons.Solid20Mini.eye,
    "skinned-mesh" to HeroIcons.Solid20Mini.sparkles,
)

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
        modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
        style = Style {
            shape(0f.dp)
            borderWidth(0f.dp)
        },
        header = {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.width(Dimension.FillMax),
            ) {
                shadcnButton(
                    id = "studio-example-rail-toggle",
                    modifier = Modifier.width(ShadcnButtonSize.Icon.heightDp.dp),
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Icon,
                    onClick = { expanded.value = !expanded.value },
                ) {
                    icon(if (expanded.value) ShadcnIcons.chevronDown else ShadcnIcons.chevronRight)
                }
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
                        glyph = ExampleIcons[example.id] ?: ShadcnIcons.cube,
                        active = active,
                        onClick = { onSelectExample(example.id) },
                    )
                }
            }
        }
    }
}

/**
 * Mirrors [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem]'s own
 * look -- that component has no leading-icon slot to extend -- with two differences: a leading
 * [glyph] in the row, and `theme.colors.accent`/`accentForeground` in place of the sidebar-
 * specific tokens (the `asShadcnTheme()` that reads those is `internal` to `ui-designsystem`,
 * unreachable from this module).
 *
 * [active] switches the variant to `Primary` instead of staying `Ghost`: `shadcnButton`'s `Ghost`
 * variant (`UiButtonVariant.resolveFill`) hardcodes its idle, non-hover fill to fully transparent
 * regardless of what a caller's own [Style] sets for the base (non-hover) state, so a persistent
 * "this is the active item" background never painted while staying `Ghost` -- confirmed
 * empirically, the active item's semantic `backgroundColor` resolved to `Color.Transparent`
 * (alpha 0) despite the `Style` block below asking for the accent fill. `Primary`
 * (`UiButtonVariant.Filled`) always honors the resolved background, and the `Style` block
 * overrides its default fill/foreground down to the accent tokens either way. Same fix
 * `IconRail.kt`'s own `railButton` already uses for its active tool, and the one landing in
 * `shadcnSidebarMenuItem` itself.
 */
private fun UiScope.exampleMenuItem(
    id: String,
    label: String,
    glyph: UiImageVector,
    active: Boolean,
    onClick: () -> Unit,
) {
    shadcnButton(
        id = id,
        modifier = Modifier.fillMaxWidth().height(32f.dp),
        variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        style = Style {
            contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
            background(if (active) theme.colors.accent else Color.Transparent)
            foreground(if (active) theme.colors.accentForeground else theme.colors.foreground)
            hovered {
                background(theme.colors.accent)
                foreground(theme.colors.accentForeground)
            }
        },
        centered = false,
        verticallyCentered = true,
        onClick = onClick,
    ) { slot ->
        row(
            horizontalArrangement = Arrangement.spacedBy(8f.dp),
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(slot.height.dp)),
        ) {
            icon(glyph, tint = if (active) theme.colors.accentForeground else theme.colors.foreground)
            shadcnText(label, modifier = Modifier.weight(1f))
        }
    }
}
