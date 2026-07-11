/*
 * Awake
 * Awake.awake-scene.iosMain
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
 * No navmesh backend on iOS yet (see docs/MMORPG_ROADMAP.md's NavMesh decision) --
 * `recast4j` is plain JVM only. iOS's real path would be Kotlin/Native cinterop against the
 * raw C++ `recastnavigation` core, deliberately deferred (no pre-built C wrapper exists for
 * it, unlike Jolt's `JoltC`). Callers must treat `null` as "no NPC/nav on this platform yet",
 * matching how `Material`/`Texture` are `TODO()`-only on the WebGPU backend already.
 */
actual fun createDemoNavMesh(): NavMesh? = null
