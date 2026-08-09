// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

import cnames.structs.VkDescriptorPool_T
import cnames.structs.VkDescriptorSetLayout_T
import cnames.structs.VkDescriptorSet_T
import cnames.structs.VkPipelineLayout_T
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorImageInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import platform.MoltenVK.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkDescriptorPoolVar
import platform.MoltenVK.VkDescriptorSetLayoutVar
import platform.MoltenVK.VkDescriptorSetVar
import platform.MoltenVK.VkDescriptorBufferInfo as NativeVkDescriptorBufferInfo
import platform.MoltenVK.VkDescriptorImageInfo as NativeVkDescriptorImageInfo
import platform.MoltenVK.VkDescriptorPoolCreateInfo as NativeVkDescriptorPoolCreateInfo
import platform.MoltenVK.VkDescriptorPoolSize as NativeVkDescriptorPoolSize
import platform.MoltenVK.VkDescriptorSetAllocateInfo as NativeVkDescriptorSetAllocateInfo
import platform.MoltenVK.VkDescriptorSetLayoutBinding as NativeVkDescriptorSetLayoutBinding
import platform.MoltenVK.VkDescriptorSetLayoutCreateInfo as NativeVkDescriptorSetLayoutCreateInfo
import platform.MoltenVK.VkWriteDescriptorSet as NativeVkWriteDescriptorSet
import platform.MoltenVK.vkAllocateDescriptorSets as nativeVkAllocateDescriptorSets
import platform.MoltenVK.vkCmdBindDescriptorSets as nativeVkCmdBindDescriptorSets
import platform.MoltenVK.vkCreateDescriptorPool as nativeVkCreateDescriptorPool
import platform.MoltenVK.vkCreateDescriptorSetLayout as nativeVkCreateDescriptorSetLayout
import platform.MoltenVK.vkDestroyDescriptorPool as nativeVkDestroyDescriptorPool
import platform.MoltenVK.vkDestroyDescriptorSetLayout as nativeVkDestroyDescriptorSetLayout
import platform.MoltenVK.vkUpdateDescriptorSets as nativeVkUpdateDescriptorSets

// Phase 6 (MoltenVK cinterop) is in progress -- see docs/MVP_PLAN.md.
@OptIn(ExperimentalForeignApi::class)
actual object VulkanDescriptors {
    actual fun vkCreateDescriptorSetLayout(
        device: Long,
        createInfo: VkDescriptorSetLayoutCreateInfo,
    ): Long = memScoped {
        val bindings = createInfo.pBindings
        val nativeCreateInfo = alloc<NativeVkDescriptorSetLayoutCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            bindingCount = (bindings?.size ?: 0).toUInt()
            pBindings = bindings?.let { bs ->
                allocArray<NativeVkDescriptorSetLayoutBinding>(bs.size) { i ->
                    binding = bs[i].binding.toUInt()
                    descriptorType = bs[i].descriptorType.toUInt()
                    descriptorCount = bs[i].descriptorCount.toUInt()
                    stageFlags = bs[i].stageFlags.toUInt()
                    pImmutableSamplers = null
                }
            }
        }
        val layoutVar = alloc<VkDescriptorSetLayoutVar>()
        val result =
            nativeVkCreateDescriptorSetLayout(device.toCPointer(), nativeCreateInfo.ptr, null, layoutVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateDescriptorSetLayout failed: $result" }
        layoutVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyDescriptorSetLayout(device: Long, layout: Long) {
        nativeVkDestroyDescriptorSetLayout(device.toCPointer(), layout.toCPointer<VkDescriptorSetLayout_T>(), null)
    }

    actual fun vkCreateDescriptorPool(device: Long, createInfo: VkDescriptorPoolCreateInfo): Long = memScoped {
        val poolSizes = createInfo.pPoolSizes
        val nativeCreateInfo = alloc<NativeVkDescriptorPoolCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            maxSets = createInfo.maxSets.toUInt()
            poolSizeCount = (poolSizes?.size ?: 0).toUInt()
            pPoolSizes = poolSizes?.let { sizes ->
                allocArray<NativeVkDescriptorPoolSize>(sizes.size) { i ->
                    type = sizes[i].type.toUInt()
                    descriptorCount = sizes[i].descriptorCount.toUInt()
                }
            }
        }
        val poolVar = alloc<VkDescriptorPoolVar>()
        val result = nativeVkCreateDescriptorPool(device.toCPointer(), nativeCreateInfo.ptr, null, poolVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateDescriptorPool failed: $result" }
        poolVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyDescriptorPool(device: Long, pool: Long) {
        nativeVkDestroyDescriptorPool(device.toCPointer(), pool.toCPointer<VkDescriptorPool_T>(), null)
    }

    actual fun vkAllocateDescriptorSet(device: Long, pool: Long, layout: Long): Long = memScoped {
        val layoutPtr = layout.toCPointer<VkDescriptorSetLayout_T>()
        val setLayouts = allocArray<CPointerVar<VkDescriptorSetLayout_T>>(1) { _: Int -> value = layoutPtr }
        val nativeAllocateInfo = alloc<NativeVkDescriptorSetAllocateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO
            pNext = null
            descriptorPool = pool.toCPointer()
            descriptorSetCount = 1u
            pSetLayouts = setLayouts
        }
        val setVar = alloc<VkDescriptorSetVar>()
        val result = nativeVkAllocateDescriptorSets(device.toCPointer(), nativeAllocateInfo.ptr, setVar.ptr)
        check(result == VK_SUCCESS) { "vkAllocateDescriptorSets failed: $result" }
        setVar.value!!.rawValue.toLong()
    }

    actual fun vkUpdateDescriptorSetBuffer(
        device: Long,
        dstSet: Long,
        dstBinding: Int,
        descriptorType: Int,
        bufferInfo: VkDescriptorBufferInfo,
    ) = memScoped {
        val nativeBufferInfo = alloc<NativeVkDescriptorBufferInfo>().apply {
            buffer = bufferInfo.buffer.toCPointer()
            offset = bufferInfo.offset.toULong()
            range = bufferInfo.range.toULong()
        }
        val write = alloc<NativeVkWriteDescriptorSet>().apply {
            sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET
            pNext = null
            this.dstSet = dstSet.toCPointer()
            this.dstBinding = dstBinding.toUInt()
            dstArrayElement = 0u
            this.descriptorType = descriptorType.toUInt()
            descriptorCount = 1u
            pImageInfo = null
            pBufferInfo = nativeBufferInfo.ptr
            pTexelBufferView = null
        }
        nativeVkUpdateDescriptorSets(device.toCPointer(), 1u, write.ptr, 0u, null)
    }

    actual fun vkUpdateDescriptorSetImage(
        device: Long,
        dstSet: Long,
        dstBinding: Int,
        descriptorType: Int,
        imageInfo: VkDescriptorImageInfo,
    ) = memScoped {
        val nativeImageInfo = alloc<NativeVkDescriptorImageInfo>().apply {
            sampler = imageInfo.sampler.toCPointer()
            imageView = imageInfo.imageView.toCPointer()
            imageLayout = imageInfo.imageLayout.toUInt()
        }
        val write = alloc<NativeVkWriteDescriptorSet>().apply {
            sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET
            pNext = null
            this.dstSet = dstSet.toCPointer()
            this.dstBinding = dstBinding.toUInt()
            dstArrayElement = 0u
            this.descriptorType = descriptorType.toUInt()
            descriptorCount = 1u
            pImageInfo = nativeImageInfo.ptr
            pBufferInfo = null
            pTexelBufferView = null
        }
        nativeVkUpdateDescriptorSets(device.toCPointer(), 1u, write.ptr, 0u, null)
    }

    actual fun vkCmdBindDescriptorSet(
        commandBuffer: Long,
        pipelineLayout: Long,
        firstSet: Int,
        descriptorSet: Long,
    ) = memScoped {
        val nativeDescriptorSet = descriptorSet.toCPointer<VkDescriptorSet_T>()
        val sets = allocArray<CPointerVar<VkDescriptorSet_T>>(1) { _: Int -> value = nativeDescriptorSet }
        nativeVkCmdBindDescriptorSets(
            commandBuffer.toCPointer(),
            platform.MoltenVK.VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipelineLayout.toCPointer<VkPipelineLayout_T>(),
            firstSet.toUInt(),
            1u,
            sets,
            0u,
            null,
        )
    }
}
