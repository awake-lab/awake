// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder

import io.github.ronjunevaldoz.awake.vulkan_generator.tool.ClassMember
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.dsl.CppClassDSL

@CppClassDSL
class CppClassBuilder(
    val className: String,
    val fileDescription: String,
    val namespace: String? = null,
    private val disableClass: Boolean = false,
    private val withInterface: Boolean = true
) {

    private val imports = mutableListOf<String>()
    val members = mutableListOf<ClassMember>()
    private val constructors = HashMap<String, String>()
    private val destructors = HashMap<String, String>()
    private val functions = HashMap<String, String>()

    fun member(accessModifier: String, type: String, name: String) {
        members.add(ClassMember(accessModifier, type, name))
    }

    fun import(import: String) {
        if (!imports.contains(import)) {
            imports.add(import)
        }
    }

    fun constructor(
        explicit: Boolean = false,
        indent: Int = 1,
        parameters: List<Pair<String, String>>,
        block: CppConstructorBuilder.() -> Unit
    ) {
        val builder =
            CppConstructorBuilder(if (withInterface) false else explicit, indent, withInterface)
        builder.parameters(parameters)
        builder.block()
        constructors[builder.buildInterface(className)] = builder.build(className)
    }

    fun destructor(
        indent: Int = 1,
        block: CppDestructorBuilder.() -> Unit
    ) {
        val builder = CppDestructorBuilder(indent, withInterface)
        builder.block()
        destructors[builder.buildInterface(className)] = builder.build(className)
    }

    fun function(
        indent: Int = 1,
        returnType: String = "void",
        name: String,
        parameters: List<Pair<String, String>> = emptyList(),
        block: CppFunctionBuilder.() -> Unit
    ) {
        val functionBuilder =
            CppFunctionBuilder(returnType, indent, className, disableClass, withInterface)
        functionBuilder.parameters(parameters)
        functionBuilder.block()
        functions[functionBuilder.buildInterface(name)] = functionBuilder.build(name)
    }

    @CppClassDSL
    inline fun <reified T : Any> function(
        indent: Int = 1,
        name: String,
        vararg parameters: Pair<String, String>,
        noinline block: CppFunctionBuilder.() -> Unit
    ) {
        val returnType = kotlinTypeToCppType<T>()
        function(indent, returnType, name, parameters.toList(), block)
    }

    fun build(): String {
        return buildString {
            append("/*\n")
            append(" *  $className.cpp\n")
            append(" *  $fileDescription\n")
            append(" *  Created by Ron June Valdoz")
            append(" */\n\n")

            for (import in imports) {
                append("#include $import\n")
            }
            if (imports.isNotEmpty()) {
                append("\n")
            }

            append("class $className")
            append(" {\n")

            for (member in members) {
                append("    ${member.accessModifier}: ${member.type} ${member.name};\n")
            }

            // by default all functions are public
            append("public:\n")
            for (constructor in constructors) {
                append(constructor.value)
                append("\n")
            }
            for (function in functions) {
                append(function.value)
                append("\n")
            }
            for (destructor in destructors) {
                append(destructor.value)
                append("\n")
            }
            // end of class
            append("};\n")
        }
    }

    fun buildClass(): String {
        return buildString {
            append("/*\n")
            append(" *  $className.cpp\n")
            append(" *  $fileDescription\n")
            append(" *  Created by Ron June Valdoz")
            append(" */\n\n")

            append("#include <includes/$className.h>\n\n")

            if (namespace != null) {
                append("namespace $namespace {\n")
            }

            for (constructor in constructors) {
                append(constructor.value)
                append("\n")
            }
            for (function in functions) {
                append(function.value)
                append("\n")
            }
            for (destructor in destructors) {
                append(destructor.value)
                append("\n")
            }

            if (namespace != null) {
                append("}\n")
            }
        }
    }

    fun buildInterface(): String {
        return buildString {
            append("/*\n")
            append(" *  $className.h\n")
            append(" *  $fileDescription\n")
            append(" *  Created by Ron June Valdoz")
            append(" */\n\n")

            val header = "${className.uppercase()}_H"

            append("#ifndef $header\n")
            append("#define $header\n\n")

            for (import in imports) {
                append("#include ${import.replace(".cpp", ".h")}\n")
            }
            if (imports.isNotEmpty()) {
                append("\n")
            }

            if (namespace != null) {
                append("namespace $namespace {\n")
            }

            if (!disableClass) {
                append("class $className")
                append(" {\n")
            }

            members.groupBy { it.accessModifier }
                .forEach {
                    append("${it.key}:\n")
                    it.value.forEach { member ->
                        append("    ${member.type} ${member.name};\n")
                    }
                }

            if (!disableClass) {
                // by default all functions are public
                append("public:\n")
            }
            for (constructor in constructors) {
                append(constructor.key)
                append("\n")
            }
            for (function in functions) {
                append(function.key)
                append("\n")
            }
            for (destructor in destructors) {
                append(destructor.key)
                append("\n")
            }
            if (!disableClass) {
                // end of class
                append("};\n")
            }
            if (namespace != null) {
                append("}\n")
            }
            append("#endif // $header")
        }
    }

    companion object {
        inline fun <reified T : Any> kotlinTypeToCppType(): String {
            return when (T::class) {
                Int::class -> "int"
                else -> "void"
            }
        }
    }
}
