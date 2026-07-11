// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder

import io.github.ronjunevaldoz.awake.vulkan_generator.tool.dsl.CppFunctionDSL

@CppFunctionDSL
class CppDestructorBuilder(private val indent: Int = 1, private val withInterface: Boolean) {
    private val body = StringBuilder()

    fun body(indent: Int = 1, functionBody: CppFunctionBodyBuilder.() -> Unit) {
        this.body.apply {
            val bodyBuilder = CppFunctionBodyBuilder(if (withInterface) 1 else indent)
            bodyBuilder.functionBody()
            append(bodyBuilder.build())
        }
    }

    fun build(className: String): String {
        val indentation = "    ".repeat(if (withInterface) 0 else indent)
        val function = if (withInterface) {
            "$className::~$className()"
        } else {
            "$indentation~$className()"
        }
        return buildString {
            append(function)
            append(" {\n")
            append(body)
            append("$indentation}\n")
        }
    }

    fun buildInterface(className: String): String {
        val indentation = "    ".repeat(indent)
        return "$indentation~$className();"
    }
}