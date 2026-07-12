// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder

import io.github.ronjunevaldoz.awake.vulkan_generator.tool.dsl.CppFunctionBodyDSL
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.dsl.CppFunctionDSL

@CppFunctionDSL
class CppFunctionBuilder(
    private val returnType: String,
    private val indent: Int = 1,
    private val className: String,
    private val disableClass: Boolean,
    private val withInterface: Boolean
) {
    private var parameters: List<Pair<String, String>> = emptyList()
    private val functionBody = StringBuilder()

    fun parameters(parameters: List<Pair<String, String>>) {
        this.parameters = parameters
    }

    @CppFunctionBodyDSL
    fun body(indent: Int = 1, functionBody: CppFunctionBodyBuilder.() -> Unit) {
        this.functionBody.apply {
            val bodyBuilder = CppFunctionBodyBuilder(if (withInterface) 1 else indent)
            bodyBuilder.functionBody()
            append(bodyBuilder.build())
        }
    }

    fun build(name: String): String {
        val indentation = "    ".repeat(if (withInterface) 0 else indent)
        val cppParameters =
            parameters.joinToString(", ") { "${it.second} ${it.first}" }
        val function = if (withInterface) {
            if (disableClass) {
                "$returnType\n$name($cppParameters)"
            } else {
                "$returnType\n$className::$name($cppParameters)"
            }
        } else {
            "$indentation$returnType $name($cppParameters)"
        }
        return buildString {
            append(function)
            append(" {\n")
            append(functionBody)
            append("$indentation}\n")
        }
    }

    fun buildInterface(name: String): String {
        val indentation = "    ".repeat(indent)
        val cppParameters = parameters.joinToString(", ") { "${it.second} ${it.first}" }
        return "$indentation$returnType $name($cppParameters);"
    }
}