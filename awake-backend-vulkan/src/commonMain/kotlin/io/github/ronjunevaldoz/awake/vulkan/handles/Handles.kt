// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.handles

import kotlin.jvm.JvmInline

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
 * `@JvmInline` keeps these zero-cost on the JVM targets (desktop/Android) -- it's required
 * there ("Value classes without '@JvmInline' annotation are not yet supported" on the JVM
 * backend). This project's Kotlin/Native (iOS) source sets currently can't resolve
 * `@JvmInline`/`@JvmOverloads` at all (see the pre-existing `@JvmOverloads` usages under
 * `models/` -- iosArm64 doesn't compile for this module yet regardless of this file; iOS
 * Vulkan support is still "planned," not implemented, per AGENTS.md). Not worked around here
 * since fixing the iOS toolchain/commonizer setup is a separate, much larger task.
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
