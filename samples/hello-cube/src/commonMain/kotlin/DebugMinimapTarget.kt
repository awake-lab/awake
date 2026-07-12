// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

/**
 * Optional per-demo contribution to [DemoCatalog]'s desktop-only debug-control WebSocket
 * channel (see `DebugControlServer.kt`, desktopMain) -- a demo (e.g. [CubeDemo]) implements
 * this to let a WebSocket client toggle its own minimap/render-target-preview feature on or
 * off deterministically, without simulating a click on the demo's `show-minimap` UI toggle.
 * Mirrors [DebugCameraTarget]/[DebugReadout]/[OffscreenPreviewSource]'s shape/rationale: not
 * part of the `Game` interface itself, most demos won't need it (e.g. [PhysicsDemo] has no
 * minimap concept), purely a sample-hello-cube debugging convenience.
 *
 * Same threading constraint as [DebugCameraTarget]: callers must only invoke [setMinimapEnabled]
 * from the same thread that owns the demo's Vulkan/scene state (see this project's
 * `.claude/AGENTS.md` "Threading model" section), never directly from a WebSocket handler
 * coroutine.
 */
interface DebugMinimapTarget {
    fun isMinimapEnabled(): Boolean
    fun setMinimapEnabled(enabled: Boolean)
}
