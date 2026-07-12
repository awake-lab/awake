// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.value
import cnames.structs.VkBuffer_T
import cnames.structs.VkCommandBuffer_T
import cnames.structs.VkDeviceMemory_T
import cnames.structs.VkDevice_T
import cnames.structs.VkPhysicalDevice_T
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkBufferVar
import platform.MoltenVK.VkDeviceMemoryVar
import platform.MoltenVK.vkAllocateMemory as nativeVkAllocateMemory
import platform.MoltenVK.vkBindBufferMemory as nativeVkBindBufferMemory
import platform.MoltenVK.vkCmdBindIndexBuffer as nativeVkCmdBindIndexBuffer
import platform.MoltenVK.vkCmdBindVertexBuffers as nativeVkCmdBindVertexBuffers
import platform.MoltenVK.vkCmdCopyBuffer as nativeVkCmdCopyBuffer
import platform.MoltenVK.vkCmdDrawIndexed as nativeVkCmdDrawIndexed
import platform.MoltenVK.vkCreateBuffer as nativeVkCreateBuffer
import platform.MoltenVK.vkDestroyBuffer as nativeVkDestroyBuffer
import platform.MoltenVK.vkDeviceWaitIdle as nativeVkDeviceWaitIdle
import platform.MoltenVK.vkFreeMemory as nativeVkFreeMemory
import platform.MoltenVK.vkGetBufferMemoryRequirements as nativeVkGetBufferMemoryRequirements
import platform.MoltenVK.vkGetPhysicalDeviceMemoryProperties as nativeVkGetPhysicalDeviceMemoryProperties
import platform.MoltenVK.vkMapMemory as nativeVkMapMemory
import platform.MoltenVK.vkUnmapMemory as nativeVkUnmapMemory
import platform.MoltenVK.VkBufferCopy as NativeVkBufferCopy
import platform.MoltenVK.VkBufferCreateInfo as NativeVkBufferCreateInfo
import platform.MoltenVK.VkMemoryAllocateInfo as NativeVkMemoryAllocateInfo
import platform.posix.memcpy

// Phase 6 (MoltenVK cinterop) is in progress -- see docs/MVP_PLAN.md.
@OptIn(ExperimentalForeignApi::class)
actual object VulkanBuffers {
    actual fun vkCreateBuffer(device: Long, createInfo: VkBufferCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkBufferCreateInfo>().apply {
            sType = platform.MoltenVK.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            size = createInfo.size.toULong()
            usage = createInfo.usage.toUInt()
            sharingMode = createInfo.sharingMode.value.toUInt()
            queueFamilyIndexCount = 0u
            pQueueFamilyIndices = null
        }
        val bufferVar = alloc<VkBufferVar>()
        val result = nativeVkCreateBuffer(device.toCPointer(), nativeCreateInfo.ptr, null, bufferVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateBuffer failed: $result" }
        bufferVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyBuffer(device: Long, buffer: Long) {
        nativeVkDestroyBuffer(device.toCPointer(), buffer.toCPointer<VkBuffer_T>(), null)
    }

    actual fun vkGetBufferMemoryRequirements(device: Long, buffer: Long): VkMemoryRequirements = memScoped {
        val native = alloc<platform.MoltenVK.VkMemoryRequirements>()
        nativeVkGetBufferMemoryRequirements(device.toCPointer(), buffer.toCPointer<VkBuffer_T>(), native.ptr)
        VkMemoryRequirements(
            size = native.size.toLong(),
            alignment = native.alignment.toLong(),
            memoryTypeBits = native.memoryTypeBits.toInt()
        )
    }

    actual fun findMemoryType(physicalDevice: Long, typeFilter: Int, properties: Int): Int = memScoped {
        val memProps = alloc<platform.MoltenVK.VkPhysicalDeviceMemoryProperties>()
        nativeVkGetPhysicalDeviceMemoryProperties(physicalDevice.toCPointer(), memProps.ptr)
        val count = memProps.memoryTypeCount.toInt()
        for (i in 0 until count) {
            val bitSet = (typeFilter and (1 shl i)) != 0
            val flagsMatch =
                (memProps.memoryTypes[i].propertyFlags.toInt() and properties) == properties
            if (bitSet && flagsMatch) {
                return@memScoped i
            }
        }
        error("findMemoryType: no suitable memory type for filter=$typeFilter properties=$properties")
    }

    actual fun vkAllocateMemory(device: Long, allocateInfo: VkMemoryAllocateInfo): Long = memScoped {
        val nativeAllocateInfo = alloc<NativeVkMemoryAllocateInfo>().apply {
            sType = platform.MoltenVK.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO
            pNext = null
            allocationSize = allocateInfo.allocationSize.toULong()
            memoryTypeIndex = allocateInfo.memoryTypeIndex.toUInt()
        }
        val memoryVar = alloc<VkDeviceMemoryVar>()
        val result = nativeVkAllocateMemory(device.toCPointer(), nativeAllocateInfo.ptr, null, memoryVar.ptr)
        check(result == VK_SUCCESS) { "vkAllocateMemory failed: $result" }
        memoryVar.value!!.rawValue.toLong()
    }

    actual fun vkFreeMemory(device: Long, memory: Long) {
        nativeVkFreeMemory(device.toCPointer(), memory.toCPointer<VkDeviceMemory_T>(), null)
    }

    actual fun vkBindBufferMemory(device: Long, buffer: Long, memory: Long, memoryOffset: Long) {
        val result = nativeVkBindBufferMemory(
            device.toCPointer(),
            buffer.toCPointer<VkBuffer_T>(),
            memory.toCPointer<VkDeviceMemory_T>(),
            memoryOffset.toULong()
        )
        check(result == VK_SUCCESS) { "vkBindBufferMemory failed: $result" }
    }

    actual fun writeBufferMemoryFloats(device: Long, memory: Long, offset: Long, data: FloatArray) = memScoped {
        val dataVar = alloc<kotlinx.cinterop.COpaquePointerVar>()
        val nativeDevice = device.toCPointer<VkDevice_T>()
        val nativeMemory = memory.toCPointer<VkDeviceMemory_T>()
        val byteSize = (data.size * Float.SIZE_BYTES).toULong()
        val result = nativeVkMapMemory(nativeDevice, nativeMemory, offset.toULong(), byteSize, 0u, dataVar.ptr)
        check(result == VK_SUCCESS) { "vkMapMemory failed: $result" }
        val dst = dataVar.value!!.reinterpret<kotlinx.cinterop.FloatVar>()
        for (i in data.indices) {
            dst[i] = data[i]
        }
        nativeVkUnmapMemory(nativeDevice, nativeMemory)
    }

    actual fun writeBufferMemoryBytes(device: Long, memory: Long, offset: Long, data: ByteArray) = memScoped {
        val dataVar = alloc<kotlinx.cinterop.COpaquePointerVar>()
        val nativeDevice = device.toCPointer<VkDevice_T>()
        val nativeMemory = memory.toCPointer<VkDeviceMemory_T>()
        val byteSize = data.size.toULong()
        val result = nativeVkMapMemory(nativeDevice, nativeMemory, offset.toULong(), byteSize, 0u, dataVar.ptr)
        check(result == VK_SUCCESS) { "vkMapMemory failed: $result" }
        data.usePinned { pinned ->
            memcpy(dataVar.value, pinned.addressOf(0), byteSize)
        }
        nativeVkUnmapMemory(nativeDevice, nativeMemory)
    }

    actual fun vkCmdBindVertexBuffers(
        commandBuffer: Long,
        firstBinding: Int,
        buffers: LongArray,
        offsets: LongArray
    ) = memScoped {
        val nativeBuffers = allocArray<CPointerVar<VkBuffer_T>>(buffers.size) { i ->
            value = buffers[i].toCPointer()
        }
        val nativeOffsets = allocArray<kotlinx.cinterop.ULongVar>(offsets.size) { i ->
            value = offsets[i].toULong()
        }
        nativeVkCmdBindVertexBuffers(
            commandBuffer.toCPointer(),
            firstBinding.toUInt(),
            buffers.size.toUInt(),
            nativeBuffers,
            nativeOffsets
        )
    }

    actual fun vkCmdBindIndexBuffer(commandBuffer: Long, buffer: Long, offset: Long, indexType: Int) {
        nativeVkCmdBindIndexBuffer(
            commandBuffer.toCPointer(),
            buffer.toCPointer<VkBuffer_T>(),
            offset.toULong(),
            indexType.toUInt()
        )
    }

    actual fun vkCmdCopyBuffer(commandBuffer: Long, srcBuffer: Long, dstBuffer: Long, size: Long) = memScoped {
        val region = alloc<NativeVkBufferCopy>().apply {
            srcOffset = 0u
            dstOffset = 0u
            this.size = size.toULong()
        }
        nativeVkCmdCopyBuffer(
            commandBuffer.toCPointer(),
            srcBuffer.toCPointer<VkBuffer_T>(),
            dstBuffer.toCPointer<VkBuffer_T>(),
            1u,
            region.ptr
        )
    }

    actual fun vkCmdDrawIndexed(
        commandBuffer: Long,
        indexCount: Int,
        instanceCount: Int,
        firstIndex: Int,
        vertexOffset: Int,
        firstInstance: Int
    ) {
        nativeVkCmdDrawIndexed(
            commandBuffer.toCPointer(),
            indexCount.toUInt(),
            instanceCount.toUInt(),
            firstIndex.toUInt(),
            vertexOffset,
            firstInstance.toUInt()
        )
    }

    actual fun vkDeviceWaitIdle(device: Long) {
        nativeVkDeviceWaitIdle(device.toCPointer())
    }

    /** Inverse of [writeBufferMemoryBytes] -- map->memcpy-out->unmap, for offscreen
     * render-target CPU readback (`Renderer.readPixels`). */
    actual fun readBufferMemoryBytes(device: Long, memory: Long, offset: Long, size: Int): ByteArray = memScoped {
        val dataVar = alloc<kotlinx.cinterop.COpaquePointerVar>()
        val nativeDevice = device.toCPointer<VkDevice_T>()
        val nativeMemory = memory.toCPointer<VkDeviceMemory_T>()
        val byteSize = size.toULong()
        val result = nativeVkMapMemory(nativeDevice, nativeMemory, offset.toULong(), byteSize, 0u, dataVar.ptr)
        check(result == VK_SUCCESS) { "vkMapMemory failed: $result" }
        val out = ByteArray(size)
        out.usePinned { pinned ->
            memcpy(pinned.addressOf(0), dataVar.value, byteSize)
        }
        nativeVkUnmapMemory(nativeDevice, nativeMemory)
        out
    }
}
