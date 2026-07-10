/*
 * Awake
 * Awake.awake-core.commonMain
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

package io.github.ronjunevaldoz.awake.core.utils

/**
 * Reads a bundled resource (e.g. "assets/shader/simple.vert") as raw bytes.
 * Replaces the removed experimental Compose `resource()` API so the engine
 * core has no dependency on Compose resource loading.
 *
 * `suspend`, not a plain blocking call, because browser resource loading (wasmJs's actual)
 * is inherently async (`fetch()`) -- see docs/MVP_PLAN.md's web-demo decision log entry.
 * The desktop/Android/iOS actuals are synchronous I/O under the hood and need no behavior
 * change, just the `suspend` keyword.
 */
expect suspend fun readResourceBytes(path: String): ByteArray
