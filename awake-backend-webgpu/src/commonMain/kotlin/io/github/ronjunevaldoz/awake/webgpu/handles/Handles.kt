/*
 * Awake
 * Awake.awake-backend-webgpu.wasmJsMain
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
