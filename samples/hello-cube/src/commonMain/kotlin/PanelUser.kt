// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.ui.ColumnScope

/**
 * Optional per-demo capability -- lets a demo append its own widgets to [DemoCatalog]'s
 * shared right-side panel column, right after the demo-picker row. Not part of the
 * `awake:engine:ui` library itself: this is one sample's own way of sharing a [ColumnScope]
 * across two `Game` instances ([DemoCatalog] + whichever demo is current), matching the
 * existing [DebugReadout]/[DebugCameraTarget]/[OffscreenPreviewSource]/[DebugMinimapTarget]
 * pattern of small, optional per-demo interfaces [DemoCatalog] checks via `as?`.
 */
interface PanelUser {
    fun drawPanel(panel: ColumnScope)
}
