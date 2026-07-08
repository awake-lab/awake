/*
 * Awake
 * Awake.awake-vulkan.desktopTest
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

import io.github.ronjunevaldoz.awake.vulkan.models.info.VkApplicationInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkInstanceCreateInfo
import kotlin.test.Test
import kotlin.test.assertNotEquals

/**
 * Phase 1b regression guard: proves the desktop native library (built via
 * `:awake-vulkan:buildDesktopNative`, loaded through `System.loadLibrary("awake-vulkan")`
 * in this module's desktopMain `Vulkan.kt`) actually links against a real Vulkan
 * implementation (MoltenVK on macOS) and can create/destroy a real VkInstance -- not just
 * that the JNI symbols resolve. Requires `-Djava.library.path` to include
 * `build/desktop-native-libs` (wired via this module's `desktopTest` task in
 * build.gradle.kts) and `buildDesktopNative` to have already run at least once.
 */
class VulkanDesktopNativeSmokeTest {
    @Test
    fun vkCreateInstance_returnsRealHandle() {
        val appInfo = VkApplicationInfo(
            pApplicationName = "Awake Desktop Smoke Test",
            pEngineName = "Awake Vulkan - Engine"
        )
        val createInfo = VkInstanceCreateInfo(pApplicationInfo = arrayOf(appInfo))
        val instance = Vulkan.vkCreateInstance(createInfo)
        assertNotEquals(0L, instance, "vkCreateInstance returned a null handle")
        Vulkan.vkDestroyInstance(instance)
    }
}
