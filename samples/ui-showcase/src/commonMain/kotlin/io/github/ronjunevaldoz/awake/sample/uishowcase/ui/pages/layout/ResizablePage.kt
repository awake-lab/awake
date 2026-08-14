// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizableHandle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanelGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height

internal val ResizablePage = ShowcasePage(
    id = "resizable",
    title = "Resizable",
    category = ShowcaseCategory.Layout,
    description = "Accessible resizable panel groups and layouts with keyboard support.",
    usageCode = """shadcnResizablePanelGroup(id = "g") { shadcnResizablePanel(...); shadcnResizableHandle(...) }""",
    referenceExample = "registry/new-york-v4/examples/resizable-demo.tsx",
    previewHeight = 420,
    notes = listOf("Nested groups compose freely -- a vertical group inside a horizontal panel."),
    hero = {
        shadcnMuted("Drag the handle to redistribute space between the two panels.")
        shadcnResizablePanelGroup(
            id = "showcase-resizable",
            modifier = Modifier.fillMaxWidth().height(240f.dp),
        ) {
            shadcnResizablePanel(id = "showcase-resizable-left", defaultSize = 0.4f) {
                shadcnText("One")
            }
            shadcnResizableHandle(id = "showcase-resizable-handle", withHandle = true)
            shadcnResizablePanel(id = "showcase-resizable-right", defaultSize = 0.6f) {
                shadcnText("Two")
            }
        }
    },
)
