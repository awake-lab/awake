// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.internal.ResizableDirection as PrimitiveDirection
import io.github.ronjunevaldoz.awake.ui.headless.internal.ResizablePanelGroupScope as PrimitiveScope
import io.github.ronjunevaldoz.awake.ui.headless.internal.resizablePanelGroup as primitiveResizablePanelGroup

enum class UiResizableDirection { Horizontal, Vertical }

class ResizablePanelGroupScope internal constructor(
    internal val primitive: PrimitiveScope,
)

fun UiScope.resizablePanelGroup(
    id: String,
    direction: UiResizableDirection = UiResizableDirection.Horizontal,
    modifier: Modifier = Modifier,
    content: ResizablePanelGroupScope.() -> Unit,
): UiBounds = primitive.primitiveResizablePanelGroup(
    id = id,
    direction = when (direction) {
        UiResizableDirection.Horizontal -> PrimitiveDirection.Horizontal
        UiResizableDirection.Vertical -> PrimitiveDirection.Vertical
    },
    modifier = modifier.asPrimitiveModifier(),
) { content(ResizablePanelGroupScope(this)) }

fun ResizablePanelGroupScope.panel(
    id: String,
    defaultSize: Float,
    minSize: Float = 0.1f,
    maxSize: Float = 1f,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.panel(
    id = id,
    defaultSize = defaultSize,
    minSize = minSize,
    maxSize = maxSize,
    content = { slot -> content(asHeadlessScope(), slot) },
)

fun ResizablePanelGroupScope.handle(
    id: String,
    withHandle: Boolean = false,
): UiBounds = primitive.handle(id = id, withHandle = withHandle)
