// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.tool

import io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder.CppClassBuilder

fun cppClass(
    className: String,
    fileDescription: String,
    namespace: String? = null,
    disableClass: Boolean = false,
    block: CppClassBuilder.() -> Unit
): Pair<String, String> {
    val builder = CppClassBuilder(className, fileDescription, namespace, disableClass)
    builder.block()
    return Pair(builder.buildInterface(), builder.buildClass())
}
