/*
 * Awake
 * Awake.awake-vulkan.wasmJsMain
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

package io.github.ronjunevaldoz.awake.vulkan.gen

// wasmJs (Phase 2.5, Web/WebGPU, decision D7) owns its canvas/surface via the browser, not
// GLFW -- same situation as iOS/Android, see VulkanWindow.kt's doc comment in commonMain.
// Scaffolding-only stub -- see io.github.ronjunevaldoz.awake.vulkan.Vulkan.kt's header
// comment in this same source set for the full rationale.
actual object VulkanWindow {
    actual fun glfwInit(): Boolean {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwTerminate() {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwWindowHint(hint: Int, value: Int) {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwCreateWindow(width: Int, height: Int, title: String): Long {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwDestroyWindow(window: Long) {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwWindowShouldClose(window: Long): Boolean {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwPollEvents() {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetFramebufferWidth(window: Long): Int {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetFramebufferHeight(window: Long): Int {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwCreateWindowSurface(instance: Long, window: Long): Long {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    // See the Android/iOS actuals' identical override for why this one is a safe no-op
    // rather than a TODO -- "no GLFW-required extensions" is true on every non-GLFW
    // platform, letting cross-platform code (GraphicsDevice.createInstance()) call this
    // unconditionally without crashing on web.
    actual fun glfwGetRequiredInstanceExtensions(): Array<String> = emptyArray()

    actual fun glfwGetKey(window: Long, key: Int): Int {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetMouseButton(window: Long, button: Int): Int {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetCursorPos(window: Long): DoubleArray {
        TODO("Not applicable on wasmJs -- see VulkanWindow.kt's doc comment.")
    }
}
