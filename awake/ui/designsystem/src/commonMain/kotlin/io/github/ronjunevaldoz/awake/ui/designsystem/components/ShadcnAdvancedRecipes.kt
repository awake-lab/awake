// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.ResizablePanelGroupScope
import io.github.ronjunevaldoz.awake.ui.headless.ScrollState
import io.github.ronjunevaldoz.awake.ui.headless.UiResizableDirection
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.handle
import io.github.ronjunevaldoz.awake.ui.headless.panel
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.resizablePanelGroup
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll

fun UiScope.shadcnResizablePanelGroup(
    id: String,
    direction: UiResizableDirection = UiResizableDirection.Horizontal,
    modifier: Modifier = Modifier,
    content: ResizablePanelGroupScope.() -> Unit,
): UiBounds = resizablePanelGroup(id = id, direction = direction, modifier = modifier, content = content)

fun ResizablePanelGroupScope.shadcnResizablePanel(
    id: String,
    defaultSize: Float,
    minSize: Float = 0.1f,
    maxSize: Float = 1f,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = panel(id, defaultSize, minSize, maxSize, content)

fun ResizablePanelGroupScope.shadcnResizableHandle(id: String, withHandle: Boolean = false): UiBounds =
    handle(id = id, withHandle = withHandle)

fun UiScope.shadcnScrollArea(
    id: String,
    modifier: Modifier = Modifier,
    state: ScrollState = rememberScrollState(id),
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier.verticalScroll(state),
    content = content,
)
