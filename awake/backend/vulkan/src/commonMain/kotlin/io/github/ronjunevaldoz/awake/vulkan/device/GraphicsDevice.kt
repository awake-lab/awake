// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.device

import io.github.ronjunevaldoz.awake.vulkan.Version
import io.github.ronjunevaldoz.awake.vulkan.Version.Companion.vkVersion
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.createSurface
import io.github.ronjunevaldoz.awake.vulkan.destroySurfaceWindow
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPhysicalDeviceType
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkApplicationInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDeviceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDeviceQueueCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkInstanceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.DebugUtilsFormattedCallback
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.VkDebugUtilsMessengerCreateInfoEXT
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDevice
import io.github.ronjunevaldoz.awake.vulkan.utils.findQueueFamilies
import io.github.ronjunevaldoz.awake.vulkan.utils.getAppExtProps
import io.github.ronjunevaldoz.awake.vulkan.utils.getAppLayerProps

/**
 * Phase 2 (renderer abstraction): owns the instance/surface/physical-device/logical-device/
 * queue lifecycle that used to be four separate private functions
 * (`createInstance`/`pickPhysicalDevice`/`createLogicalDevice`/`setupDebugMessenger`) plus
 * seven scattered fields directly on the demo's `VulkanApplication`. Extracted verbatim --
 * same calls, same order, same hazards already discovered and documented (MoltenVK
 * Portability detection, queue-family completeness check) -- so this is a structural move,
 * not a behavior change.
 */
class GraphicsDevice {
    var instance: Long = 0
    var debugUtilsMessenger: Long = 0
    var surface: Long = 0
    var physicalDevice: Long = 0
    var device: Long = 0
    var graphicsQueue: Long = 0
    var presentQueue: Long = 0

    private var nativeWindow: Any? = null

    /** [window] is an `android.view.Surface` on Android, or a GLFW window handle (`Long`)
     * on desktop -- see [io.github.ronjunevaldoz.awake.vulkan.createSurface]. */
    fun create(window: Any) {
        createInstance()
        setupDebugMessenger()
        nativeWindow = window
        surface = createSurface(instance, window)
        pickPhysicalDevice()
        createLogicalDevice()
    }

    private fun createInstance() {
        val appInfo = VkApplicationInfo(
            pApplicationName = "Awake Vulkan - Application",
            pEngineName = "Awake Vulkan - Engine",
            apiVersion = Version(1, 3, 0).vkVersion
        )
        val layerProperties = getAppLayerProps()
        val layerExtProps = layerProperties.map { layer ->
            getAppExtProps(layer)
        }.flatten()

        // glfwGetRequiredInstanceExtensions() is a safe no-op returning emptyArray() on
        // every non-GLFW platform (Android/iOS) -- see VulkanWindow.kt's actuals.
        val glfwExtensions = VulkanWindow.glfwGetRequiredInstanceExtensions().toList()
        // MoltenVK (desktop macOS) conforms to the Vulkan Portability spec: vkCreateInstance
        // requires both VK_KHR_portability_enumeration enabled AND
        // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR set, or it fails with
        // VK_ERROR_INCOMPATIBLE_DRIVER. GLFW reporting VK_EXT_metal_surface as required is
        // the reliable signal we're really running on MoltenVK (as opposed to Android or a
        // real-native-Vulkan desktop driver, neither of which ever reports that extension).
        val onMoltenVk = "VK_EXT_metal_surface" in glfwExtensions
        val portabilityExtension = if (onMoltenVk) listOf("VK_KHR_portability_enumeration") else emptyList()
        val instanceFlags = if (onMoltenVk) 0x00000001 else 0 // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR

        val extProperties = (getAppExtProps() + layerExtProps + glfwExtensions + portabilityExtension).distinct()

        val createInfo = VkInstanceCreateInfo(
            flags = instanceFlags,
            pApplicationInfo = arrayOf(appInfo),
            ppEnabledLayerNames = layerProperties.toTypedArray(),
            ppEnabledExtensionNames = extProperties.toTypedArray()
        )
        instance = Vulkan.vkCreateInstance(createInfo)
    }

    private fun setupDebugMessenger() {
        val androidLogCallback: (String, String) -> Unit = { severity, message ->
            println("AWAKE_VERIFY_VALIDATION [$severity] $message")
        }
        val createInfo = VkDebugUtilsMessengerCreateInfoEXT(
            pfnUserCallback = { severity, messageType, callbackData, userData ->
                DebugUtilsFormattedCallback(androidLogCallback).invoke(
                    severity,
                    messageType,
                    callbackData,
                    userData
                )
            },
            pUserData = null
        )
        debugUtilsMessenger = Vulkan.vkCreateDebugUtilsMessengerEXT(instance, createInfo)
    }

    private fun pickPhysicalDevice() {
        val physicalDevices =
            Vulkan.vkEnumeratePhysicalDevices(instance).map { VkPhysicalDevice(it, instance) }
        if (physicalDevices.isNotEmpty()) {
            // find a gpu
            val gpu = physicalDevices.find { vkDevice ->
                val properties = Vulkan.vkGetPhysicalDeviceProperties(vkDevice.physicalDevice)
                val features = Vulkan.vkGetPhysicalDeviceFeatures(vkDevice.physicalDevice)
                val hasGeometry = features.geometryShader
                val isIntegratedGPU =
                    properties.deviceType == VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU
                val isDiscreteGPU =
                    properties.deviceType == VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU
                val isVirtualGPU =
                    properties.deviceType == VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU
                isIntegratedGPU || isDiscreteGPU || isVirtualGPU
            } ?: throw Exception("Cannot find suitable gpu!")
            physicalDevice = gpu.physicalDevice
        }
    }

    private fun createLogicalDevice() {
        // Queue families
        val indices = findQueueFamilies(physicalDevice, surface)

        if (!indices.isComplete()) {
            // graphics not supported?
            throw Exception("GPU graphics / Presentation not supported")
        }

        // to avoid duplicate queue family index use set
        val uniqueQueueFamilies = setOf(
            indices.graphicsFamily!!,
            indices.presentFamily!!
        )
        val queueInfos = uniqueQueueFamilies.map { uniqueQueueFamilyIndex ->
            VkDeviceQueueCreateInfo(
                queueFamilyIndex = uniqueQueueFamilyIndex,
                queueCount = 1,
                pQueuePriorities = floatArrayOf(1.0f)
            )
        }

        val features = Vulkan.vkGetPhysicalDeviceFeatures(physicalDevice)
        val deviceExtensions =
            Vulkan.vkEnumerateDeviceExtensionProperties(physicalDevice)
                .map { it.extensionName }.toList()
        val layerProperties = getAppLayerProps()
        val layerDeviceExtProps = layerProperties.map { layer ->
            Vulkan.vkEnumerateDeviceExtensionProperties(physicalDevice, layer)
                .map { it.extensionName }.toList()
        }.flatten()

        val deviceInfo = VkDeviceCreateInfo(
            pQueueCreateInfos = queueInfos.toTypedArray(),
            pEnabledFeatures = arrayOf(features),
            ppEnabledExtensionNames = (deviceExtensions + layerDeviceExtProps).distinct()
                .toTypedArray()
        )
        device = Vulkan.vkCreateDevice(physicalDevice, deviceInfo)

        graphicsQueue = Vulkan.vkGetDeviceQueue(device, indices.graphicsFamily!!, 0)
        presentQueue = Vulkan.vkGetDeviceQueue(device, indices.presentFamily!!, 0)
    }

    fun destroy() {
        Vulkan.vkDestroySurfaceKHR(instance, surface)
        Vulkan.vkDestroyDevice(device)
        Vulkan.vkDestroyDebugUtilsMessengerEXT(instance, debugUtilsMessenger)
        Vulkan.vkDestroyInstance(instance)
        nativeWindow?.let { destroySurfaceWindow(it) }
    }
}
