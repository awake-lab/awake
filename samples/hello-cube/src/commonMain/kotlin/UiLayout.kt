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

const val PANEL_ROW_DEMO_PICKER_Y = 20f
const val PANEL_ROW_DEBUG_TOGGLE_Y = 60f
const val PANEL_ROW_CAMERA_MODE_Y = 108f
const val PANEL_ROW_FRUSTUM_Y = 148f
const val PANEL_ROW_GRID_Y = 186f
const val PANEL_ROW_MINIMAP_Y = 224f
const val PANEL_ROW_AZIMUTH_Y = 266f
const val PANEL_ROW_ELEVATION_Y = 302f
const val PANEL_ROW_ZOOM_Y = 338f
