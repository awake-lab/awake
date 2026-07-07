/*
 * Awake
 * Awake.awake-vulkan-generator.main
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.vulkan_generator.vulkan

import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.FileWriter
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.JNIType
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder.CppClassBuilder
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder.CppFunctionBodyBuilder
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.cast
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.cppClass
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getArrayElementJavaType
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getVkArray
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getVkConstArray
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.setArrayRegion
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.setJavaValue
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.toJavaSignature
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.toJavaType
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.toJavaTypeArray
import java.lang.reflect.Field

fun createVulkanMutator(clazz: Class<*>) {
    if (!clazz.isAnnotationPresent(VkMutator::class.java)) {
        return
    }
    val cppClassCode =
        cppClass(clazz.simpleName + "Mutator", "Vulkan mutator for ${clazz.simpleName}") {
            createImports()
            createMembers(clazz)
            createDestructor()
            createConstructor(clazz)
            createToObjectFunction(clazz)
        }

    val awakeVulkanCpp = "awake-vulkan/src/main/cpp/vulkan-kotlin"
    FileWriter.writeFile(
        "$awakeVulkanCpp/includes/${clazz.simpleName + "Mutator"}.h",
        cppClassCode.first
    )
    FileWriter.writeFile("$awakeVulkanCpp/${clazz.simpleName + "Mutator"}.cpp", cppClassCode.second)
}

fun CppClassBuilder.createImports() {
    import("<jni.h>")
    import("<vulkan/vulkan.h>")
    import("<string>")
    import("<vector>")
    import("<enum_utils.h>")
}

fun CppClassBuilder.createMembers(clazz: Class<*>) {
    val declareMembers = clazz.declaredFields
    member("private", "JNIEnv*", "env")
    member("private", "jclass", "clazz")
    declareMembers.forEach { member ->
        member("private", "jfieldID", member.name + "Field")
    }
}

fun CppClassBuilder.createDestructor() {
    destructor {
        // default destructor
        body(2) {
            child("// env->DeleteGlobalRef(clazz);")
        }
    }
}

fun CppClassBuilder.createConstructor(clazz: Class<*>) {
    val declareMembers = clazz.declaredFields
    constructor(
        true,
        1, listOf(
            Pair("env", "JNIEnv*"),
        )
    ) {
        body(2) {
            child("this->env = env;")
            members.forEach { member ->
                if (member.type == "jfieldID") {
                    val javaField =
                        declareMembers.find { it.name + "Field" == member.name }!!
                    val javaSig = "\"${javaField.toJavaSignature()}\""
                    val sourceClass = "clazz"
                    val fieldName = "\"${javaField.name}\""
                    child("${member.name} = env->GetFieldID($sourceClass, $fieldName, $javaSig);")
                } else if (member.type == "jclass") {
                    val javaClassSig = "\"${clazz.name.replace(".", "/")}\""
                    child("${member.name} = env->FindClass($javaClassSig);")
                }
            }
        }
    }
}

fun CppClassBuilder.createToObjectFunction(clazz: Class<*>) {
    val declareMembers = clazz.declaredFields
    function(
        1, "jobject", "toObject",
        listOf(Pair("source", clazz.simpleName))
    ) {
        body(2) {
            // return clazz info
            child("auto constructor = env->GetMethodID(clazz, \"<init>\", \"()V\");")
            child("auto newObj = env->NewObject(clazz, constructor);")
            declareMembers.forEach { javaMember ->
                when {
                    javaMember.type.isArray -> processMutatorArray(
                        javaMember,
                        import = { import(it) })

                    javaMember.type.isEnum -> processMutatorEnum(javaMember)
                    // int, byte, short, long, float, double, boolean and char
                    javaMember.type.isPrimitive -> processMutatorPrimitive(javaMember)
                    // Object, Any, Void, String
                    else -> processMutatorObject(javaMember, import = { import(it) })
                }
            }
            child("return newObj;")
        }
    }
}

fun CppFunctionBodyBuilder.processMutatorObject(
    javaMember: Field,
    import: (dependency: String) -> Unit
) {
    val fieldIdName = javaMember.name + "Field"
    val objName = javaMember.name
    if (javaMember.toJavaType() == JNIType.JString) {
        child("// process string")
        child("auto $objName = env->NewStringUTF(source.$objName);")
    } else if (javaMember.type.simpleName.startsWith("vk", true)) {
        if (javaMember.type.isArray) {
            import("<includes/${javaMember.type.componentType.simpleName}Mutator.h>")
            child("${javaMember.type.componentType.simpleName}Mutator ${objName}Mutator(env);")
            child("auto $objName = ${objName}Mutator.toObject(source.$objName);")
        } else {
            import("<includes/${javaMember.type.simpleName}Mutator.h>")
            child("${javaMember.type.simpleName}Mutator ${javaMember.name}Mutator(env);")
            child("auto $objName = ${objName}Mutator.toObject(source.$objName);")
        }
    } else {
        child("// processing Any, Void, Null, Object")
        child("auto $objName = (jobject) source.$objName;")
    }
    child("env->SetObjectField(newObj, $fieldIdName, $objName);")
    child("env->DeleteLocalRef($objName);")
}


fun CppFunctionBodyBuilder.processMutatorPrimitive(javaMember: Field) {
    val fieldIdName = javaMember.name + "Field"
    if (javaMember.toJavaType() == JNIType.JChar) {
        child("// process char")
        child("auto ${javaMember.name} = env->NewStringUTF(source.${javaMember.name});")
        child("env->SetObjectField(newObj, $fieldIdName, ${javaMember.name});")
    } else {
        val functionName = javaMember.cast("source." + javaMember.name)
        val assignPrimitiveValue = javaMember.setJavaValue(
            "env",
            "newObj",
            fieldIdName,
            functionName
        )
        child("$assignPrimitiveValue;")
    }
}

fun CppFunctionBodyBuilder.processMutatorArray(
    javaMember: Field,
    import: (dependency: String) -> Unit
) {
    val fieldIdName = javaMember.name + "Field"
    if (javaMember.type.componentType.isPrimitive) {
        child("// processing primitive array")
        val arraySize =
            javaMember.getVkConstArray()?.arraySize ?: "<NO ARRAY SIZE>"
        val buffer =
            "reinterpret_cast<${javaMember.getArrayElementJavaType()} *>(source.${javaMember.name} )"
        child("${javaMember.toJavaTypeArray()}  ${javaMember.name} = env->New${javaMember.type.componentType.simpleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}Array($arraySize);")
        child(
            "env->" + javaMember.setArrayRegion(
                javaMember.name,
                "0",
                arraySize,
                buffer
            )
        )
        child("env->SetObjectField(newObj, $fieldIdName, ${javaMember.name});")
        child("env->DeleteLocalRef(${javaMember.name});")
    } else {
        child("// processing non-primitive array")
        val javaArray = javaMember.getVkArray()
        if (javaArray != null) {
            val vulkanArrayName = "${javaMember.name}Array"

            import("<includes/${javaMember.type.componentType.simpleName}Mutator.h>")
            child(
                "jclass ${javaMember.name}Clazz = env->FindClass(\"${
                    javaMember.type.componentType.name.replace(
                        ".",
                        "/"
                    )
                }\");"
            )
            child("${javaMember.toJavaTypeArray()} $vulkanArrayName = env->NewObjectArray(source.${javaArray.sizeAlias}, ${javaMember.name}Clazz, nullptr);")
            child("for (int i = 0; i < source.${javaArray.sizeAlias}; ++i) {")
            child("    auto element = source.${javaMember.name}[i];")
            child("    auto obj = ${javaMember.type.componentType.simpleName}Mutator(env).toObject(element);")
            child("    env->SetObjectArrayElement($vulkanArrayName, i, obj);")
            child("    env->DeleteLocalRef(obj);")
            child("}")
            child("env->SetObjectField(newObj, $fieldIdName, $vulkanArrayName);")
            child("env->DeleteLocalRef($vulkanArrayName);")
            child("env->DeleteLocalRef(${javaMember.name}Clazz);")
        }
    }
}

fun CppFunctionBodyBuilder.processMutatorEnum(javaMember: Field) {
    val fieldIdName = javaMember.name + "Field"
    val functionName = javaMember.cast("source." + javaMember.name)
    val assignEnumValue = javaMember.setJavaValue(
        "env",
        "newObj",
        fieldIdName,
        javaMember.name
    )
    child("auto ${javaMember.name} = $functionName;")
    child("$assignEnumValue;")
    child("env->DeleteLocalRef(${javaMember.name});")
}