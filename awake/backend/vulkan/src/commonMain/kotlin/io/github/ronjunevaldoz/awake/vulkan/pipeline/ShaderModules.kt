// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkShaderModuleCreateInfo

/** Wraps compiled SPIR-V [code] in a `VkShaderModule` -- was copy-pasted byte-for-byte across
 * every pipeline class (`RenderPipeline`/`ShadowRenderPipeline`/`LineRenderPipeline`/
 * `SkyboxRenderPipeline`, plus 4 UI render pipelines); this is the shared version they all
 * call. */
internal fun createShaderModule(device: Long, code: IntArray): Long =
    Vulkan.vkCreateShaderModule(device, VkShaderModuleCreateInfo(pCode = code))

/** Repacks 4-byte-aligned SPIR-V bytes into the `IntArray` `VkShaderModuleCreateInfo.pCode`
 * expects -- same copy-paste history as [createShaderModule] above. */
internal fun ByteArray.toShaderIntArray(): IntArray = IntArray(size / 4) { i ->
    (this[i * 4].toInt() and 0xFF) or
        ((this[i * 4 + 1].toInt() and 0xFF) shl 8) or
        ((this[i * 4 + 2].toInt() and 0xFF) shl 16) or
        ((this[i * 4 + 3].toInt() and 0xFF) shl 24)
}
