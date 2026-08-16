// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnButtonGroupOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroupColumn
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroupSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons

private val outline = HeroIcons.Outline24

internal val ButtonGroupPage = ShowcasePage(
    id = "button-group",
    title = "Button Group",
    category = ShowcaseCategory.Inputs,
    description = "Buttons joined into a single control, sharing an outer border and corner radius.",
    usageCode = """
shadcnButtonGroup(id = "group-demo") {
    shadcnButton(id = "btn-1", label = "Left", variant = ShadcnButtonVariant.Ghost)
    shadcnButtonGroupSeparator("sep-1")
    shadcnButton(id = "btn-2", label = "Right", variant = ShadcnButtonVariant.Ghost)
}
""".trimIndent(),
    referenceExample = "registry/new-york-v4/examples/button-group-demo.tsx",
    notes = listOf(
        "Supports unified orientation control via ShadcnButtonGroupOrientation (Horizontal / Vertical).",
        "Uses LocalShadcnButtonGroup context to automatically strip inner button corner radii and handle vertical width expansion.",
        "Use shadcnButtonGroupSeparator() to render context-aware hairline dividers between buttons.",
    ),
    hero = {
        var selectedAction by rememberStateValue("btn-group-hero", "action") { "Save" }
        column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
            shadcnButtonGroup(id = "button-group-hero") {
                shadcnButton(
                    id = "group-btn-save",
                    label = "Save",
                    variant = if (selectedAction == "Save") ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                    onClick = { selectedAction = "Save" },
                )

                shadcnButtonGroupSeparator("sep-hero-1")

                shadcnButton(
                    id = "group-btn-play",
                    label = "Play",
                    variant = if (selectedAction == "Play") ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                    onClick = { selectedAction = "Play" },
                )

                shadcnButtonGroupSeparator("sep-hero-2")

                shadcnButton(
                    id = "group-btn-console",
                    label = "Console",
                    variant = if (selectedAction == "Console") ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                    onClick = { selectedAction = "Console" },
                )
            }
            shadcnMuted("Selected action: $selectedAction")
        }
    },
    variants = {
        column(verticalArrangement = Arrangement.spacedBy(16f.dp)) {
            shadcnText("Horizontal Group (Toolbar / Segmented Control)")
            shadcnButtonGroup(id = "button-group-horizontal") {
                shadcnButton(
                    id = "btn-horiz-select",
                    variant = ShadcnButtonVariant.Primary,
                    size = ShadcnButtonSize.Icon,
                ) {
                    icon(
                        outline.cursorArrowRays,
                        modifier = Modifier.size(16f.dp),
                        tint = ShadcnTheme.colors.primaryForeground,
                    )
                }
                shadcnButtonGroupSeparator("sep-horiz-1")
                shadcnButton(
                    id = "btn-horiz-move",
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Icon,
                ) {
                    icon(
                        outline.arrowsPointingOut,
                        modifier = Modifier.size(16f.dp),
                        tint = ShadcnTheme.colors.foreground,
                    )
                }
                shadcnButtonGroupSeparator("sep-horiz-2")
                shadcnButton(
                    id = "btn-horiz-rotate",
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Icon,
                ) {
                    icon(
                        outline.arrowPath,
                        modifier = Modifier.size(16f.dp),
                        tint = ShadcnTheme.colors.foreground,
                    )
                }
                shadcnButtonGroupSeparator("sep-horiz-3")
                shadcnButton(
                    id = "btn-horiz-scale",
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Icon,
                ) {
                    icon(
                        outline.arrowsPointingIn,
                        modifier = Modifier.size(16f.dp),
                        tint = ShadcnTheme.colors.foreground,
                    )
                }
            }

            spacer(Modifier.height(8f.dp))

            shadcnText("Vertical Group (Unified Orientation Parameter)")
            shadcnButtonGroup(
                id = "button-group-vertical",
                orientation = ShadcnButtonGroupOrientation.Vertical,
                modifier = Modifier.width(120f.dp),
            ) {
                shadcnButton(
                    id = "btn-vert-top",
                    label = "Top",
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                )
                shadcnButtonGroupSeparator("sep-vert-1")
                shadcnButton(
                    id = "btn-vert-center",
                    label = "Center",
                    variant = ShadcnButtonVariant.Primary,
                    size = ShadcnButtonSize.Sm,
                )
                shadcnButtonGroupSeparator("sep-vert-2")
                shadcnButton(
                    id = "btn-vert-bottom",
                    label = "Bottom",
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                )
            }
        }
    },
    states = {
        column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
            shadcnText("Subtle / Secondary Variant Group")
            shadcnButtonGroup(id = "button-group-secondary") {
                shadcnButton(
                    id = "btn-sec-1",
                    label = "Option A",
                    variant = ShadcnButtonVariant.Secondary,
                    size = ShadcnButtonSize.Sm,
                )
                shadcnButtonGroupSeparator("sep-sec-1")
                shadcnButton(
                    id = "btn-sec-2",
                    label = "Option B",
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                )
                shadcnButtonGroupSeparator("sep-sec-2")
                shadcnButton(
                    id = "btn-sec-3",
                    label = "Option C",
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                )
            }
        }
    },
)
