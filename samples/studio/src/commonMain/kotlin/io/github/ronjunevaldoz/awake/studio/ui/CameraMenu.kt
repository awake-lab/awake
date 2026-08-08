// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuEntry
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.rememberPopupState

/** Camera mode + projection picker shared by the viewport's right-click menu and the icon
 * rail's camera dropdown, so both list and dispatch identically. */
private sealed interface CameraMenuAction {
    data class Mode(val mode: StudioContract.CameraPresetMode) : CameraMenuAction
    data class Projection(val projection: StudioContract.Projection) : CameraMenuAction
}

// 1:1 with CameraMenuItems below, separator excluded -- shadcnDropdownMenu's own
// selectedIndex/actionIndex numbering already skips separators the same way.
private val CameraMenuActions: List<CameraMenuAction> = listOf(
    CameraMenuAction.Mode(StudioContract.CameraPresetMode.Orbit),
    CameraMenuAction.Mode(StudioContract.CameraPresetMode.Front),
    CameraMenuAction.Mode(StudioContract.CameraPresetMode.Top),
    CameraMenuAction.Projection(StudioContract.Projection.Perspective),
    CameraMenuAction.Projection(StudioContract.Projection.Orthographic),
)

internal val CameraMenuItems: List<UiDropdownMenuEntry> = listOf(
    UiDropdownMenuItem(label = "Orbit"),
    UiDropdownMenuItem(label = "Front"),
    UiDropdownMenuItem(label = "Top"),
    UiDropdownMenuSeparator,
    UiDropdownMenuItem(label = "Perspective"),
    UiDropdownMenuItem(label = "Orthographic"),
)

/** Maps a picked item index to the matching narrowed callback -- keeps this file store-agnostic
 * (this module's panels take a value + a callback, never the whole store). */
internal fun dispatchCameraMenuPick(
    index: Int,
    onSelectMode: (StudioContract.CameraPresetMode) -> Unit,
    onSelectProjection: (StudioContract.Projection) -> Unit,
) {
    when (val action = CameraMenuActions.getOrNull(index) ?: return) {
        is CameraMenuAction.Mode -> onSelectMode(action.mode)
        is CameraMenuAction.Projection -> onSelectProjection(action.projection)
    }
}

/**
 * Right-click camera menu anchored at the cursor, hit-tested against an already-claimed
 * [bounds] (the caller's own viewport slot -- kept as a plain parameter, not a `target: UiScope.
 * () -> UiBounds` content lambda, so the caller's own `RowScope.column()`/`Modifier.weight()`
 * call keeps resolving to the row-aware overload it needs; see that call site).
 *
 * `shadcnContextMenu` (ui-designsystem) opens the same way on a secondary click, but reports no
 * picked item back to its caller -- no return value, no per-item callback on
 * [UiDropdownMenuItem] -- so this composes the same public primitives it uses internally
 * (hover + secondary click -> [shadcnDropdownMenu] anchored at the click point) directly here,
 * to get [onPick]'s index back.
 */
internal fun UiScope.viewportCameraMenu(
    id: String,
    bounds: UiBounds,
    onPick: (Int) -> Unit,
) {
    val input = context.inputState
    val state = widgetState(id)
    val popup = rememberPopupState(id)
    val isHovered = input.pointerX >= bounds.x && input.pointerX <= bounds.x + bounds.width &&
        input.pointerY >= bounds.y && input.pointerY <= bounds.y + bounds.height

    if (isHovered && input.secondaryPointerDown) {
        state.set("clickX", input.pointerX)
        state.set("clickY", input.pointerY)
        if (!popup.expanded) popup.open()
    }

    if (popup.expanded) {
        val clickX = state.get("clickX", input.pointerX)
        val clickY = state.get("clickY", input.pointerY)
        val result = shadcnDropdownMenu(
            id = "$id.menu",
            anchorSlot = UiBounds(clickX, clickY, 0f, 0f),
            expanded = true,
            items = CameraMenuItems,
            width = Dimension.WrapContent,
        )
        if (result.dismissed || result.selectedIndex != null) popup.close()
        result.selectedIndex?.let(onPick)
    }
}
