/*
 * Awake
 * Awake.awake-scene.wasmJsMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.scene.navigation

/**
 * No navmesh backend on wasmJs yet (see docs/MMORPG_ROADMAP.md's NavMesh decision) --
 * `recast4j` is plain JVM only. wasmJs's real path would be the community WASM port
 * `recast-navigation-js` via JS interop (the same role `wgpu4k` plays for WebGPU today),
 * deliberately deferred. Callers must treat `null` as "no NPC/nav on this platform yet".
 */
actual fun createDemoNavMesh(): NavMesh? = null
