// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkApplicationInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkInstanceCreateInfo

/**
 * Manual verification for GLFW window + Vulkan surface creation on desktop -- run via
 * `./gradlew :awake-backend-vulkan:verifyGlfwMain`. Deliberately NOT a `desktopTest` JUnit test:
 * on macOS, Cocoa requires window creation on the process's real OS main thread, which a
 * Gradle test worker doesn't run on (`glfwCreateWindow` crashed with SIGTRAP from a test
 * process, confirmed empirically) -- `verifyGlfwMain`'s `JavaExec` does run on the real
 * main thread. Same manual-diagnostic-task convention as `checkJniBindings`.
 */
fun main() {
    check(VulkanWindow.glfwInit()) { "glfwInit failed" }
    VulkanWindow.glfwWindowHint(0x00022001, 0) // GLFW_CLIENT_API, GLFW_NO_API
    val window = VulkanWindow.glfwCreateWindow(64, 64, "Awake Desktop Verify")
    check(window != 0L) { "glfwCreateWindow returned null" }
    val w = VulkanWindow.glfwGetFramebufferWidth(window)
    val h = VulkanWindow.glfwGetFramebufferHeight(window)
    println("AWAKE_VERIFY framebuffer size: ${w}x$h")

    // MoltenVK conforms to the Vulkan Portability spec: vkCreateInstance requires both
    // the VK_KHR_portability_enumeration extension AND the
    // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR flag, or it fails with
    // VK_ERROR_INCOMPATIBLE_DRIVER (confirmed empirically).
    val requiredExtensions = VulkanWindow.glfwGetRequiredInstanceExtensions() + "VK_KHR_portability_enumeration"
    println("AWAKE_VERIFY required extensions: ${requiredExtensions.toList()}")

    val appInfo = VkApplicationInfo(pApplicationName = "Awake Desktop Verify", pEngineName = "Awake Vulkan - Engine")
    val instance = Vulkan.vkCreateInstance(
        VkInstanceCreateInfo(
            flags = 0x00000001, // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR
            pApplicationInfo = arrayOf(appInfo),
            ppEnabledExtensionNames = requiredExtensions,
        ),
    )
    check(instance != 0L) { "vkCreateInstance returned null" }
    println("AWAKE_VERIFY instance handle: $instance")

    val surface = VulkanWindow.glfwCreateWindowSurface(instance, window)
    check(surface != 0L) { "glfwCreateWindowSurface returned null" }
    println("AWAKE_VERIFY surface handle: $surface")

    Vulkan.vkDestroySurfaceKHR(instance, surface)
    Vulkan.vkDestroyInstance(instance)
    VulkanWindow.glfwDestroyWindow(window)
    VulkanWindow.glfwTerminate()
    println("AWAKE_VERIFY: all cleanup completed without throwing")
}
