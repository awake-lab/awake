// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder

import io.github.ronjunevaldoz.awake.vulkan_generator.tool.dsl.CppFunctionBodyDSL

@CppFunctionBodyDSL
class CppFunctionBodyBuilder(private val indent: Int) {
    private val bodyContent = StringBuilder()

    fun child(line: String) {
        val indentation = "    ".repeat(indent)
        bodyContent.append("$indentation$line\n")
    }

    fun build(): String {
        return bodyContent.toString()
    }
}