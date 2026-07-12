// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.navigation

/**
 * No navmesh backend on iOS yet (see docs/MMORPG_ROADMAP.md's NavMesh decision) --
 * `recast4j` is plain JVM only. iOS's real path would be Kotlin/Native cinterop against the
 * raw C++ `recastnavigation` core, deliberately deferred (no pre-built C wrapper exists for
 * it, unlike Jolt's `JoltC`). Callers must treat `null` as "no NPC/nav on this platform yet",
 * matching how `Material`/`Texture` are `TODO()`-only on the WebGPU backend already.
 */
actual fun createDemoNavMesh(): NavMesh? = null
