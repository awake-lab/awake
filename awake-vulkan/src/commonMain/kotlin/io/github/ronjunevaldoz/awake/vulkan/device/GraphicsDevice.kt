/*
 * Awake
 * Awake.awake-vulkan.commonMain
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

package io.github.ronjunevaldoz.awake.vulkan.device

/**
 * Phase 2.5 (Web/WebGPU, decision D7) milestone 1: this became an `expect class` so a future
 * WebGPU actual can implement the same seam -- see docs/MVP_PLAN.md. The real Vulkan
 * implementation (desktop/Android/iOS, unchanged from before this split) now lives in
 * `vulkanMain/.../device/GraphicsDevice.kt`; `wasmJsMain`'s actual is a `TODO()` stub.
 *
 * [window] is an `android.view.Surface` on Android, or a GLFW window handle (`Long`) on
 * desktop -- see [io.github.ronjunevaldoz.awake.vulkan.createSurface].
 */
expect class GraphicsDevice() {
    var instance: Long
    var debugUtilsMessenger: Long
    var surface: Long
    var physicalDevice: Long
    var device: Long
    var graphicsQueue: Long
    var presentQueue: Long

    fun create(window: Any)
    fun destroy()
}
