/*
 * Awake
 * Awake.awake-vulkan.desktopMain
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

package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

actual fun createSurface(instance: Long, window: Any): Long =
    VulkanWindow.glfwCreateWindowSurface(instance, window as Long)

actual fun destroySurfaceWindow(window: Any) {
    val handle = window as Long
    VulkanWindow.glfwDestroyWindow(handle)
    VulkanWindow.glfwTerminate()
}
