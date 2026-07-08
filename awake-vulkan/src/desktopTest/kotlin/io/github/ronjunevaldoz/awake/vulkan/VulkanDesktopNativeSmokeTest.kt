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
 *
 * GLFW window/surface creation ([io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow])
 * is deliberately NOT exercised here: on macOS, Cocoa requires all window creation to
 * happen on the process's real OS main thread, which a Gradle test worker process does
 * not run on -- `glfwCreateWindow` from this test process crashed with SIGTRAP (confirmed
 * empirically, not just AppKit documentation). Verified instead via a real `fun main()`
 * entry point, which the JVM does run on the OS main thread -- see
 * docs/decisions/D10-codegen-derisk-findings.md for that verification.
 *
 * Also the CI smoke test on Linux (see .github/workflows/ci.yml's desktop-vulkan-smoke
 * job): a plain `libvulkan.so` + Mesa's lavapipe (`mesa-vulkan-drivers`) is a real,
 * fully-conformant Vulkan implementation, not a Portability-subset one like MoltenVK, so
 * this test only requests the Portability bits on macOS.
 */
class VulkanDesktopNativeSmokeTest {
    @Test
    fun vkCreateInstance_returnsRealHandle() {
        val appInfo = VkApplicationInfo(
            pApplicationName = "Awake Desktop Smoke Test",
            pEngineName = "Awake Vulkan - Engine"
        )
        // MoltenVK conforms to the Vulkan Portability spec: vkCreateInstance requires both
        // VK_KHR_portability_enumeration and VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR,
        // or it fails with VK_ERROR_INCOMPATIBLE_DRIVER (confirmed empirically once this
        // module switched from linking MoltenVK directly to the real Vulkan loader). Linux's
        // Vulkan loader + lavapipe doesn't support (or need) this KHR_portability extension.
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        val createInfo = VkInstanceCreateInfo(
            flags = if (isMac) 0x00000001 else 0, // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR
            pApplicationInfo = arrayOf(appInfo),
            ppEnabledExtensionNames = if (isMac) arrayOf("VK_KHR_portability_enumeration") else emptyArray()
        )
        val instance = Vulkan.vkCreateInstance(createInfo)
        assertNotEquals(0L, instance, "vkCreateInstance returned a null handle")
        Vulkan.vkDestroyInstance(instance)
    }
}
