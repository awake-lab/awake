// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.handles

import kotlin.jvm.JvmInline

/**
 * Local copy of `awake-backend-vulkan`'s `handles/Handles.kt` (Module restructuring slice 2,
 * see docs/MVP_PLAN.md) -- these 9 tiny value classes are duplicated rather than shared
 * across a module dependency, since the whole point of physically splitting the Vulkan and
 * WebGPU backends is that neither depends on the other. [WebGpuHandles] uses these the same
 * way the Vulkan backend's real handle-owning classes do, just wrapping a table index instead
 * of a raw Vulkan handle.
 */
@JvmInline
value class BufferHandle(val handle: Long)

@JvmInline
value class DeviceMemoryHandle(val handle: Long)

@JvmInline
value class ImageHandle(val handle: Long)

@JvmInline
value class ImageViewHandle(val handle: Long)

@JvmInline
value class SamplerHandle(val handle: Long)

@JvmInline
value class DescriptorSetLayoutHandle(val handle: Long)

@JvmInline
value class DescriptorPoolHandle(val handle: Long)

@JvmInline
value class DescriptorSetHandle(val handle: Long)

@JvmInline
value class CommandPoolHandle(val handle: Long)
