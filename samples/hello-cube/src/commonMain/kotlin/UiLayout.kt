// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

/**
 * Shared right-side debug-panel column geometry. [DemoCatalog]'s demo-picker dropdown and
 * whichever [io.github.ronjunevaldoz.awake.engine.application.Game] is current (e.g.
 * [CubeDemo]'s camera-mode/slider/toggle widgets) both anchor their rows to this same column
 * so the whole panel reads as one vertical stack, not two unrelated overlays that happen to
 * share a screen edge. Fixed per-row pixel constants (not a shared mutable layout cursor)
 * match this codebase's existing "explicit pixel geometry per widget" convention rather than
 * introducing a new flow-layout abstraction for a handful of rows.
 */
const val PANEL_WIDTH = 200f
const val PANEL_MARGIN = 20f
fun panelX(viewportWidth: Float): Float = viewportWidth - PANEL_WIDTH - PANEL_MARGIN

// Non-expanding rows (toggles/sliders) come first, both dropdowns last -- a UiContext
// dropdown draws its option list BELOW its own header when expanded (one row per option,
// same row height as the header), so placing a dropdown above other fixed-position widgets
// meant its expanded list landed directly on top of them. Putting both dropdowns at the
// bottom, with enough gap for each one's own max expansion (2 options here), means an open
// dropdown only ever overlaps empty space below the panel, not another widget.
const val PANEL_ROW_DEBUG_TOGGLE_Y = 20f
const val PANEL_ROW_FRUSTUM_Y = 68f
const val PANEL_ROW_GRID_Y = 106f
const val PANEL_ROW_MINIMAP_Y = 144f
const val PANEL_ROW_AZIMUTH_Y = 186f
const val PANEL_ROW_ELEVATION_Y = 222f
const val PANEL_ROW_ZOOM_Y = 258f
// 2 options * 32f row height below its own header -- next row must start past that.
const val PANEL_ROW_DEMO_PICKER_Y = 296f
const val PANEL_ROW_CAMERA_MODE_Y = 400f
