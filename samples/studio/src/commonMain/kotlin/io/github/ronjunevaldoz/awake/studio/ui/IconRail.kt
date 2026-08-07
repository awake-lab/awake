// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.components.icon
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width

/** Top-level section switcher, separate from [drawExampleRail]'s list -- the rail picks
 * which area of the app is showing, the list panel next to it shows that area's content.
 * v1 has exactly one real section ("Examples"); "Assets"/"Settings" render as disabled,
 * icon-less stub slots rather than being omitted, so the rail reads as a real app shell
 * without a wrong or duplicated icon standing in for content that doesn't exist yet. */
internal fun RowScope.drawIconRail() {
    column(
        id = "studio-icon-rail",
        verticalArrangement = Arrangement.spacedBy(4f.dp),
        modifier = Modifier.width(56f.dp).height(Dimension.FillMax).padding(8f.dp),
    ) {
        shadcnButton(id = "studio-rail-examples", enabled = true) {
            icon(ShadcnIcons.squares2x2)
        }
        shadcnButton(id = "studio-rail-assets", enabled = false) { }
        shadcnButton(id = "studio-rail-settings", enabled = false) { }
    }
}
