// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTooltip
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.modifier.width

internal val TooltipPage = ShowcasePage(
    id = "tooltip",
    title = "Tooltip",
    category = ShowcaseCategory.Overlays,
    description = "A popup that displays information related to an element when it receives focus or hover.",
    usageCode = """shadcnTooltip(id = "hint", anchorSlot = trigger.slot, visible = true) { shadcnText("Hint") }""",
    referenceExample = "registry/new-york-v4/examples/tooltip-demo.tsx",
    previewHeight = 460,
    notes = listOf("shadcnTooltipText is the text-only convenience wrapper over the same composition."),
    hero = {
        shadcnMuted("A tooltip is a tiny overlay, but it still needs proper container chrome, spacing, and wrap behavior.")
        spacer(Modifier.height(8f.dp))
        row(
            horizontalArrangement = Arrangement.spacedBy(12f.dp),
            modifier = Modifier.height(36f.dp),
        ) {
            shadcnButton(
                id = "showcase-tooltip-trigger",
                modifier = Modifier.width(156f.dp).height(36f.dp),
            ) { trigger ->
                text("Hover target", centered = true)
                uiScope().shadcnTooltip(
                    id = "showcase-tooltip",
                    anchorSlot = trigger,
                    visible = true,
                    width = Dimension.Fixed(260f.dp),
                    positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
                ) {
                    shadcnText("Scene stats stay live here when the cursor rests on the trigger.")
                }
            }
            shadcnButton(
                id = "showcase-tooltip-secondary",
                label = "Reference",
                modifier = Modifier.width(120f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Secondary,
            )
        }
        spacer(Modifier.height(12f.dp))
        row(
            horizontalArrangement = Arrangement.spacedBy(12f.dp),
            modifier = Modifier.height(36f.dp),
        ) {
            shadcnButton(
                id = "showcase-tooltip-text-trigger",
                modifier = Modifier.width(200f.dp).height(36f.dp),
            ) { trigger ->
                text("Text-only tooltip", centered = true)
                uiScope().shadcnTooltipText(
                    id = "showcase-tooltip-text",
                    anchorSlot = trigger,
                    visible = true,
                    text = "shadcnTooltipText skips the custom content lambda for a plain wrapped label.",
                    positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
                )
            }
        }
    },
)
