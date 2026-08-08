// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.layout

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizableHandle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanelGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.ResizableDirection
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** shadcn's own `resizable-demo-with-handle` reference: a horizontal group split 50/50, whose
 * right panel is itself a vertical group split 25/75. */
private fun UiScope.resizableDemoLabel(label: String) {
    text(
        label = label,
        modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
        centered = true,
        verticallyCentered = true,
    )
}

internal fun ColumnScope.drawShadcnResizableDemoPreview() {
    shadcnBadge("RESIZABLE", variant = ShadcnBadgeVariant.Outline)
    shadcnSupportingText("Drag a handle to redistribute space between the two panels touching it.")
    spacer(Modifier.height(8f.dp))

    shadcnResizablePanelGroup(
        id = "showcase-resizable",
        direction = ResizableDirection.Horizontal,
        modifier = Modifier.width(360f.dp).height(200f.dp),
    ) {
        shadcnResizablePanel(id = "showcase-resizable-one", defaultSize = 0.5f) {
            resizableDemoLabel("One")
        }
        shadcnResizableHandle(id = "showcase-resizable-h1", withHandle = true)
        shadcnResizablePanel(id = "showcase-resizable-two-three", defaultSize = 0.5f) {
            shadcnResizablePanelGroup(
                id = "showcase-resizable-inner",
                direction = ResizableDirection.Vertical,
            ) {
                shadcnResizablePanel(id = "showcase-resizable-two", defaultSize = 0.25f) {
                    resizableDemoLabel("Two")
                }
                shadcnResizableHandle(id = "showcase-resizable-h2", withHandle = true)
                shadcnResizablePanel(id = "showcase-resizable-three", defaultSize = 0.75f) {
                    resizableDemoLabel("Three")
                }
            }
        }
    }
}
