// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Vec3

/**
 * Optional per-demo contribution to [DemoCatalog]'s desktop-only debug-control WebSocket
 * channel (see `DebugControlServer.kt`, desktopMain) -- a demo (e.g. [CubeDemo],
 * [PhysicsDemo]) implements this to expose its own live camera for an AI agent (or any other
 * WebSocket client) to read/mutate deterministically, without simulating mouse drags on the
 * demo's orbit/free-fly camera controls. Mirrors [DebugReadout]/[OffscreenPreviewSource]'s
 * shape/rationale: not part of the `Game` interface itself, most demos won't need it, purely
 * a sample-hello-cube debugging convenience.
 *
 * Mutations here go straight through to the demo's live `Camera` component -- callers (i.e.
 * `DebugControlServer`'s command-queue drain in `Main.kt`) must only invoke these from the
 * same thread that owns the demo's Vulkan/scene state (see this project's
 * `.claude/AGENTS.md` "Threading model" section), never directly from a WebSocket handler
 * coroutine.
 */
interface DebugCameraTarget {
    fun getCameraEye(): Vec3
    fun setCameraEye(eye: Vec3)
    fun getCameraCenter(): Vec3
    fun setCameraCenter(center: Vec3)
}
