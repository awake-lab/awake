// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.device

import io.github.ronjunevaldoz.awake.vulkan.Version
import io.github.ronjunevaldoz.awake.vulkan.Version.Companion.vkVersion
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.createSurface
import io.github.ronjunevaldoz.awake.vulkan.destroySurfaceWindow
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPhysicalDeviceType
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkDebugUtilsMessageSeverityFlagBitsEXT
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkApplicationInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDeviceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDeviceQueueCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkInstanceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.DebugUtilsFormattedCallback
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.VkDebugUtilsMessengerCreateInfoEXT
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDevice
import io.github.ronjunevaldoz.awake.vulkan.utils.QueueFamilyIndices
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
    private var failOnValidationError = false
    private val validationErrors = mutableListOf<String>()

    /** [window] is an `android.view.Surface` on Android, or a GLFW window handle (`Long`)
     * on desktop -- see [io.github.ronjunevaldoz.awake.vulkan.createSurface]. */
    fun create(window: Any) {
        createInstance()
        setupDebugMessenger()
        nativeWindow = window
        surface = createSurface(instance, window)
        pickPhysicalDevice()
        val indices = findQueueFamilies(physicalDevice, surface)
        if (!indices.isComplete()) {
            // graphics not supported?
            throw Exception("GPU graphics / Presentation not supported")
        }
        createLogicalDevice(indices)
    }

    /** Desktop-only headless variant of [create] for pure offscreen rendering (no window, no
     * `VkSurfaceKHR`, no swapchain -- see `Renderer`'s createHeadless doc comment and
     * docs/MVP_PLAN.md's pixel-baseline-testing entry). `surface` stays `0L`
     * (`VK_NULL_HANDLE`) for this instance's whole lifetime: [findQueueFamilies] already
     * treats that as "skip present-family detection" (see its own doc comment), so only a
     * graphics-capable queue family is required here, and that same family backs both
     * [graphicsQueue]/[presentQueue] (the latter is never actually used without a swapchain
     * to present to). */
    fun createHeadless() {
        failOnValidationError = true
        createInstance(includeGlfwExtensions = false)
        setupDebugMessenger()
        pickPhysicalDevice()
        val graphicsFamily = requireNotNull(findQueueFamilies(physicalDevice, surface).graphicsFamily) {
            "GPU graphics queue not supported"
        }
        createLogicalDevice(QueueFamilyIndices(graphicsFamily, graphicsFamily))
    }

    /** [includeGlfwExtensions] is `false` for [createHeadless]: `glfwGetRequiredInstanceExtensions`
     * requires `glfwInit()` to have already run, which is NOT safe to call from a plain JVM test
     * thread on macOS (Cocoa requires GLFW's whole lifecycle on the real OS main thread -- see
     * `GlfwManualVerify.kt`'s doc comment; confirmed empirically for `glfwCreateWindow`, and not
     * worth risking for `glfwInit` either) -- a headless [GraphicsDevice] never creates a window
     * or surface, so it doesn't need GLFW's platform-surface instance extensions anyway. */
    private fun createInstance(includeGlfwExtensions: Boolean = true) {
        val appInfo = VkApplicationInfo(
            pApplicationName = "Awake Vulkan - Application",
            pEngineName = "Awake Vulkan - Engine",
            apiVersion = Version(1, 3, 0).vkVersion
        )
        val layerProperties = getAppLayerProps()
        val layerExtProps = layerProperties.map { layer ->
            getAppExtProps(layer)
        }.flatten()
        val baseExtProperties = (getAppExtProps() + layerExtProps).distinct()

        // glfwGetRequiredInstanceExtensions() is a safe no-op returning emptyArray() on
        // every non-GLFW platform (Android/iOS) -- see VulkanWindow.kt's actuals.
        val glfwExtensions = if (includeGlfwExtensions) VulkanWindow.glfwGetRequiredInstanceExtensions().toList() else emptyList()
        // MoltenVK (desktop macOS) conforms to the Vulkan Portability spec: vkCreateInstance
        // requires both VK_KHR_portability_enumeration enabled AND
        // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR set, or it fails with
        // VK_ERROR_INCOMPATIBLE_DRIVER. Detected via the loader's OWN globally-queried instance
        // extension list (baseExtProperties) reporting VK_KHR_portability_enumeration as
        // available -- not GLFW's required-extensions list (which needs glfwInit() to have run,
        // unavailable to [createHeadless]) -- since the loader already reports this whenever a
        // portability-subset ICD (MoltenVK) is registered, GLFW or not.
        val onMoltenVk = "VK_KHR_portability_enumeration" in baseExtProperties
        val portabilityExtension = if (onMoltenVk) listOf("VK_KHR_portability_enumeration") else emptyList()
        val instanceFlags = if (onMoltenVk) 0x00000001 else 0 // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR

        val extProperties = (baseExtProperties + glfwExtensions + portabilityExtension).distinct()

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
                if (severity.isValidationError()) {
                    validationErrors += callbackData.pMessage
                }
                false
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

    private fun createLogicalDevice(indices: QueueFamilyIndices) {
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
        val availableDeviceExtensions =
            Vulkan.vkEnumerateDeviceExtensionProperties(physicalDevice)
                .map { it.extensionName }
                .toSet()
        // Deliberately NOT also querying vkEnumerateDeviceExtensionProperties(physicalDevice,
        // layerName) per-layer (as an earlier version of this function did) -- device-level
        // layers are a deprecated Vulkan 1.0 concept the loader/ICD already ignore in
        // practice (a device's real extension list is layer-independent), and querying them
        // per-layer is actively broken on at least this project's own dev/CI setup: found
        // (2026-07-12, D24 minimap-crash investigation) that as soon as ANY instance layer
        // is discoverable on the host (e.g. `brew install vulkan-validationlayers`, which on
        // macOS ends up scanned even without VK_LAYER_PATH set), this call fails loader-side
        // with "pLayerName is too long or is badly formed", which cascades into
        // `vkCreateDevice` itself failing with VK_ERROR_EXTENSION_NOT_PRESENT -- i.e. the
        // app couldn't even start. Skipping the per-layer query entirely avoids this without
        // losing anything real (the plain, no-layer `deviceExtensions` query above already
        // returns the physical device's actual extension list).
        val requiredDeviceExtensions = buildList {
            if (surface != 0L) add("VK_KHR_swapchain")
            if ("VK_KHR_portability_subset" in availableDeviceExtensions) {
                add("VK_KHR_portability_subset")
            }
        }
        val missingDeviceExtensions = requiredDeviceExtensions.filterNot(availableDeviceExtensions::contains)
        require(missingDeviceExtensions.isEmpty()) {
            "Missing Vulkan device extensions: ${missingDeviceExtensions.joinToString()}"
        }

        val deviceInfo = VkDeviceCreateInfo(
            pQueueCreateInfos = queueInfos.toTypedArray(),
            pEnabledFeatures = arrayOf(features),
            ppEnabledExtensionNames = requiredDeviceExtensions.toTypedArray()
        )
        device = Vulkan.vkCreateDevice(physicalDevice, deviceInfo)

        graphicsQueue = Vulkan.vkGetDeviceQueue(device, indices.graphicsFamily!!, 0)
        presentQueue = Vulkan.vkGetDeviceQueue(device, indices.presentFamily!!, 0)
    }

    fun destroy() {
        if (surface != 0L) Vulkan.vkDestroySurfaceKHR(instance, surface)
        if (device != 0L) Vulkan.vkDestroyDevice(device)
        if (debugUtilsMessenger != 0L) Vulkan.vkDestroyDebugUtilsMessengerEXT(instance, debugUtilsMessenger)
        if (instance != 0L) Vulkan.vkDestroyInstance(instance)
        nativeWindow?.let { destroySurfaceWindow(it) }
        if (failOnValidationError && validationErrors.isNotEmpty()) {
            error(
                "Vulkan validation errors were reported:\n" +
                    validationErrors.joinToString(separator = "\n\n")
            )
        }
    }

    private fun VkDebugUtilsMessageSeverityFlagBitsEXT.isValidationError(): Boolean =
        this == VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
}
