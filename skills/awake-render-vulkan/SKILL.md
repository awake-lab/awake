---
name: awake-render-vulkan
description: >
  Rules and invariants for Awake's Vulkan rendering backend (`awake:backend:vulkan` and its JNI/C++ bindings).
  Read before touching Vulkan swapchain creation/resizing, GPU resource allocations, command recording,
  JNI bindings generator, or Android Vulkan verification. Trigger keywords - Vulkan, VkSwapchainKHR, VkDevice,
  VkImage, VkBuffer, vkCmdBindPipeline, vkQueueSubmit, VulkanView, Android Vulkan, MoltenVK, JNI bindings.
---

# Vulkan Backend Engineering in Awake

Awake's Vulkan backend (`:awake:backend:vulkan`, `:awake:backend:vulkan:bindings`, and `:awake:backend:vulkan:bindings:android-native`) provides direct, high-performance hardware rendering without large engine runtime overhead.

Read [docs/architecture.md](../../docs/architecture.md), [docs/MVP_PLAN.md](../../docs/MVP_PLAN.md), and [skills/awake-render-pipeline/SKILL.md](../awake-render-pipeline/SKILL.md) first.

## 1. Resource Lifetime Symmetry

Every Vulkan creation path must have an explicit, symmetrical destruction path:
- `VkDevice` / `VkInstance` / `VkSwapchainKHR` destruction ordering must match standard Vulkan spec rules.
- Texture and buffer objects must track their memory allocations (`VkDeviceMemory` / VMA allocator) and destroy them symmetrically when unbind/dispose occurs.
- Never rely on JVM finalizers or GC to release GPU handles.

## 2. JNI Bindings & Generated Code Contract

- `awake:backend:vulkan:bindings` contains ~126 generated files (`models/`, `enums/`) and 6 hand-authored root files (`Vulkan.kt`, `Common.kt`, `Annotations.kt`, `Flags.kt`, `VulkanSurface.kt`, `Version.kt`).
- **Rule**: Never hand-edit files in `models/` or `enums/` or the C++ JNI accessors. Always run the code generator in `:awake:backend:vulkan:generator`.
- Check [`awake/backend/vulkan/README.md`](../../awake/backend/vulkan/README.md) before modifying binding layers.

## 3. Swapchain & Surface Resize Behavior

- Window resize triggers `VulkanSwapchain` recreation.
- Ensure the backing surface is properly queried for `minImageCount`, `currentExtent`, and supported format/color space before recreating the swapchain.
- Avoid the "backing surface never resized" failure mode by synchronizing surface dimensions with the native window size on each frame boundary.
- For swapchain fundamentals, consult [vulkan-tutorial.com](https://vulkan-tutorial.com/).

## 4. Verification & Regression Gate

- **Android Vulkan is the primary regression gate**: Android device testing catches alignment, memory qualification, and synchronization bugs that desktop drivers may silently tolerate.
- **Headless Pixel Baselines**: When touching shader pipelines or draw submissions, run headless pixel-baseline tests (`PixelBaseline.kt`) to verify frame output determinism.
- **Frame-Timing Harness**: When modifying command buffer recording or state transitions, verify frame timing metrics using `TimingBaseline` in `awake:ui:testing`.
