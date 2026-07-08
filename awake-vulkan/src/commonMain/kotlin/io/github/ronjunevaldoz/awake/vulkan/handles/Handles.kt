/*
 * Awake
 * Awake.awake-vulkan.commonMain
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

package io.github.ronjunevaldoz.awake.vulkan.handles

/**
 * Typed wrappers around the raw `Long` Vulkan handles that Awake's own engine classes
 * (`Mesh`, `Texture`, `Material`, `TransferContext`, ...) expose as public API, so a
 * `sampler` can't be passed where an `imageView` is expected -- both are `Long` at the JNI
 * boundary and the compiler can't tell them apart otherwise. Named with a `Handle` suffix
 * (`BufferHandle`, not `VkBuffer`) specifically to avoid colliding with the code generator's
 * own per-file `typealias VkXxx = VkHandle` convention (see `common.kt`'s `VkHandle`) --
 * those remain plain `Long` and stay that way, since retyping the generated
 * `Vulkan*`/`gen` JNI layer itself is out of scope here (that's the generator's contract,
 * not this module's).
 *
 * `@JvmInline` keeps these zero-cost on the JVM targets (desktop/Android); on non-JVM
 * targets a `value class` already compiles to the underlying type with no wrapper overhead.
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
