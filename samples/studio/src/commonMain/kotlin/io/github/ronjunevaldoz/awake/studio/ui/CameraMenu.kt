// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuEntry
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.headless.contextMenuTrigger

/** Camera mode + projection picker shared by the viewport's right-click menu and the icon
 * rail's camera dropdown, so both list and dispatch identically. */
private sealed interface CameraMenuAction {
    data class Mode(val mode: CameraMode) : CameraMenuAction
    data class Projection(val projection: StudioContract.Projection) : CameraMenuAction
}

// 1:1 with CameraMenuItems below, separator excluded -- shadcnDropdownMenu's own
// selectedIndex/actionIndex numbering already skips separators the same way.
private val CameraMenuActions: List<CameraMenuAction> = listOf(
    CameraMenuAction.Mode(CameraMode.ThirdPerson),
    CameraMenuAction.Mode(CameraMode.FirstPerson),
    CameraMenuAction.Mode(CameraMode.Cinematic),
    CameraMenuAction.Mode(CameraMode.TopDown),
    CameraMenuAction.Projection(StudioContract.Projection.Perspective),
    CameraMenuAction.Projection(StudioContract.Projection.Orthographic),
)

internal val CameraMenuItems: List<ShadcnDropdownMenuEntry> = listOf(
    ShadcnDropdownMenuItem(label = "Third Person"),
    ShadcnDropdownMenuItem(label = "First Person"),
    ShadcnDropdownMenuItem(label = "Cinematic"),
    ShadcnDropdownMenuItem(label = "Top Down"),
    ShadcnDropdownMenuSeparator,
    ShadcnDropdownMenuItem(label = "Perspective"),
    ShadcnDropdownMenuItem(label = "Orthographic"),
)

/** Maps a picked item index to the matching narrowed callback -- keeps this file store-agnostic
 * (this module's panels take a value + a callback, never the whole store). */
internal fun dispatchCameraMenuPick(
    index: Int,
    onSelectMode: (CameraMode) -> Unit,
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
 * [ShadcnDropdownMenuItem] -- so this composes the same public primitives it uses internally
 * (hover + secondary click -> [shadcnDropdownMenu] anchored at the click point) directly here,
 * to get [onPick]'s index back.
 */
internal fun UiScope.viewportCameraMenu(
    id: String,
    bounds: UiBounds,
    onPick: (Int) -> Unit,
) {
    val popup = rememberPopupState(id)
    val trigger = contextMenuTrigger(id = id, expanded = popup.expanded, target = bounds)
    if (trigger.shouldOpen) popup.open()
    if (popup.expanded) {
        val result = shadcnDropdownMenu(
            id = "$id.menu",
            anchorSlot = trigger.anchor,
            expanded = true,
            items = CameraMenuItems,
            width = Dimension.WrapContent,
        )
        if (result.dismissed || result.selectedIndex != null) popup.close()
        result.selectedIndex?.let(onPick)
    }
}

/**
 * The icon rail's camera dropdown -- the same list as [viewportCameraMenu], opened by a normal
 * click on the rail button rather than a secondary click on the viewport.
 *
 * Shares [CameraMenuItems] and [dispatchCameraMenuPick] with the viewport menu, so the two cannot
 * drift in what they offer or how they dispatch. Only the trigger differs, which is why this is a
 * separate function rather than a flag on the other one.
 */
internal fun UiScope.railCameraMenu(
    id: String,
    anchor: UiBounds,
    requestOpen: Boolean,
    onPick: (Int) -> Unit,
) {
    val popup = rememberPopupState(id)
    if (requestOpen) popup.open()
    if (popup.expanded) {
        val result = shadcnDropdownMenu(
            // ".dropdown", so items land on "$id.dropdown.item.N" -- the ids the rail's callers
            // and StudioModuleCameraTest address. viewportCameraMenu uses ".menu" for its own.
            id = "$id.dropdown",
            anchorSlot = anchor,
            expanded = true,
            items = CameraMenuItems,
            width = Dimension.WrapContent,
        )
        if (result.dismissed || result.selectedIndex != null) popup.close()
        result.selectedIndex?.let(onPick)
    }
}
