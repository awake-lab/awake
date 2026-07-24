// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.clip as coreClip

@Deprecated(
    message = "Compatibility clip alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.core.graphics.clip or the ui-core clip helpers directly."
)
fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) = coreClip(rect, content)

@Deprecated(
    message = "Compatibility clip alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.core.graphics.clip or the ui-core clip helpers directly."
)
fun UiScope.clip(path: UiPath, content: UiScope.() -> Unit) = coreClip(path, content)

@Deprecated(
    message = "Compatibility clip alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.core.graphics.clip or the ui-core clip helpers directly."
)
fun UiScope.clip(shape: UiShapeSpec, rect: UiSlot, content: UiScope.() -> Unit) =
    coreClip(shape, rect, content)
