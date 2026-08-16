// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("use VkArray instead")
annotation class VkPointer

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class VkConstArray(val arraySize: String = "")

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class VkHandleRef(val name: String)

@Retention(AnnotationRetention.RUNTIME)
annotation class VkReturnType(val name: String)

annotation class NativeSurfaceWindow

/**
 *
 *         auto pfnDestroyDebugUtilsMessengerEXT =
 *                 (PFN_vkDestroyDebugUtilsMessengerEXT) vkGetInstanceProcAddr(
 *                         instance, "vkDestroyDebugUtilsMessengerEXT");
 *
 */
annotation class VkSingleton
annotation class VkUnionMember(val alias: String, val saveToParent: Boolean = false)

/**
 * @param sizeAlias <elementType> [sizeAlias] otherwise do not generate
 * @param stride multiplier for array size for example given UInt::class -> sizeOf(uint32_t)
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class VkArray(
    val sizeAlias: String = "",
    val stride: KClass<*> = Nothing::class,
)

/**
 * Allows to generate toObject cpp method
 */
@Target(AnnotationTarget.CLASS)
annotation class VkMutator
