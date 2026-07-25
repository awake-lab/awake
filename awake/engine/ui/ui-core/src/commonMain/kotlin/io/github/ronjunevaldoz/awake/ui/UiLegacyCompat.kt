// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.graphics.clip as coreClip
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.spacer as extSpacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface as extSurface
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

// Compatibility aliases slated for future removal, kept only so `import
// io.github.ronjunevaldoz.awake.ui.*` call sites resolve surface/clip/spacer without also
// importing io.github.ronjunevaldoz.awake.ui.layouts.*/io.github.ronjunevaldoz.awake.ui.graphics.*.
// Prefer importing the real functions directly from their owning packages.

@Deprecated(
    message = "Compatibility clip alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.graphics.clip."
)
fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) = coreClip(rect, content)

@Deprecated(
    message = "Compatibility clip alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.graphics.clip."
)
fun UiScope.clip(shape: UiShapeSpec, rect: UiSlot, content: UiScope.() -> Unit) =
    coreClip(shape, rect, content)

@Deprecated(
    message = "Compatibility surface alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.layouts.surface."
)
fun UiScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

@Deprecated(
    message = "Compatibility surface alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.layouts.surface."
)
fun ColumnScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

@Deprecated(
    message = "Compatibility surface alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.layouts.surface."
)
fun RowScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

@Deprecated(
    message = "Compatibility surface alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.layouts.surface."
)
fun AbsoluteScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

@Deprecated(
    message = "Compatibility surface alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.layouts.surface."
)
fun BoxScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

@Deprecated(
    message = "Compatibility spacer alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.layouts.spacer."
)
fun ColumnScope.spacer(modifier: UiModifier) = extSpacer(modifier)

@Deprecated(
    message = "Compatibility spacer alias slated for future removal. Prefer io.github.ronjunevaldoz.awake.ui.layouts.spacer."
)
fun RowScope.spacer(modifier: UiModifier) = extSpacer(modifier)
