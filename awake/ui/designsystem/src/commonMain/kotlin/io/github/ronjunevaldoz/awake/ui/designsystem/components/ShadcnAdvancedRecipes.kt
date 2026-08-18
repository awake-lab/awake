// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
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
import io.github.ronjunevaldoz.awake.ui.style.Style

fun UiScope.shadcnResizablePanelGroup(
    id: String,
    direction: UiResizableDirection = UiResizableDirection.Horizontal,
    modifier: UiModifier = Modifier,
    content: ResizablePanelGroupScope.() -> Unit,
): UiBounds = resizablePanelGroup(id = id, direction = direction, modifier = modifier, content = content)

fun ResizablePanelGroupScope.shadcnResizablePanel(
    id: String,
    defaultSize: Float,
    minSize: Float = 0.1f,
    maxSize: Float = 1f,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = panel(id, defaultSize, minSize, maxSize, content)

fun ResizablePanelGroupScope.shadcnResizableHandle(
    id: String,
    withHandle: Boolean = false,
    style: Style = Style.Empty,
): UiBounds = handle(id = id, withHandle = withHandle, style = style)

fun UiScope.shadcnScrollArea(
    id: String,
    modifier: UiModifier = Modifier,
    state: ScrollState = rememberScrollState(id),
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    // A scrolling `surface()` call (one with `.scrollState` on its modifier) routes to
    // `scrollPanel()` (see `ScrollContainers.kt`), not `resolveVisualSurface()` -- that path
    // already stopped reading `theme.components.surface` before this session (its own doc there:
    // "a bare scroll container should stay invisible by default"), so unlike
    // `shadcnInputOTP`'s wrapper this one's look was never actually theme-dependent. Explicit
    // Style.Empty here is a no-op vs. before, not a reproduction of anything.
    modifier = modifier.verticalScroll(state),
    style = Style.Empty,
    content = content,
)
