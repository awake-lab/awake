// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.navigation

import io.github.ronjunevaldoz.awake.scene.navigation.NavMesh

/**
 * No navmesh backend on wasmJs yet (see docs/MMORPG_ROADMAP.md's NavMesh decision) --
 * `recast4j` is plain JVM only. wasmJs's real path would be the community WASM port
 * `recast-navigation-js` via JS interop (the same role `wgpu4k` plays for WebGPU today),
 * deliberately deferred. Callers must treat `null` as "no NPC/nav on this platform yet".
 */
internal actual fun createScene3DDemoNavMesh(): NavMesh? = null
