// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.vulkan

import io.github.ronjunevaldoz.awake.vulkan.NativeSurfaceWindow
import io.github.ronjunevaldoz.awake.vulkan.VkUnionMember
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.FileWriter
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.JNIType
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder.CppClassBuilder
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.builder.CppFunctionBodyBuilder
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.cppClass
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getArrayElement
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getArrayElementJavaType
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getArrayRegion
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getJavaValue
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getObjectJavaValue
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getVkArray
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getVkConstArray
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.getVkHandle
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.isVkConstArray
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.isVkHandle
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.isVkPointer
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.javaTypeSuffix
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.toJavaSignature
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.toJavaType
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.toJavaTypeArray
import io.github.ronjunevaldoz.awake.vulkan_generator.tool.toVulkanType
import java.lang.reflect.Field
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses

fun createVulkanAccessor(clazz: Class<*>) {
    val cppClassCode =
        if (clazz.kotlin.isSealed) {
            cppClass(clazz.simpleName + "Accessor", "") {
                import("<jni.h>")
                member("private", "JNIEnv*", "env")
                member("private", "jobject", "obj")

                constructor(
                    indent = 1,
                    parameters = listOf(Pair("env", "JNIEnv*"), Pair("obj", "jobject")),
                ) {
                    body(2) {
//                        child("this->env = env;")
//                        child("this->obj = env->NewGlobalRef(obj);")
                    }
                }
                function(1, "void", "fromObject", listOf(Pair("info", "${clazz.simpleName}&"))) {
                    body(2) {
                        clazz.kotlin.sealedSubclasses.forEach { subClass ->
                            import("<includes/${subClass.simpleName}Accessor.h>")
                            val classSig = subClass.qualifiedName?.replace(".", "/")?.replace(
                                "/" + subClass.simpleName.toString(),
                                "$" + subClass.simpleName.toString(),
                            )
                            child("auto ${subClass.simpleName} = env->FindClass(\"${classSig}\");")
                            child("if(env->IsInstanceOf(obj, ${subClass.simpleName})) {")
                            child("    ${subClass.simpleName}Accessor(env, obj).fromObject(info);")
                            child("}")
                            child("env->DeleteLocalRef(${subClass.simpleName});")
                        }
                    }
                }
            }
        } else {
            val declareMembers = clazz.declaredFields
            val className = clazz.simpleName
            cppClass(className + "Accessor", "Vulkan accessor for ${clazz.simpleName}") {
                import("<jni.h>")
                import("<vulkan/vulkan.h>")
                import("<string>")
                import("<vector>")
                import("<enum_utils.h>")

                member("private", "JNIEnv*", "env")
                member("private", "jobject", "obj")
                member("private", "jclass", "clazz")

                declareMembers.forEach { member ->
                    member("private", "jfieldID", member.name + "Field")
                }

                destructor {
                    // default destructor
                    body(2) {
//                        child("env->DeleteGlobalRef(obj);")
                        child("env->DeleteLocalRef(clazz);")
                    }
                }

                constructor(
                    false,
                    1,
                    listOf(
                        Pair("env", "JNIEnv*"),
                        Pair("obj", "jobject"),
                    ),
                ) {
                    body(2) {
//                        child("this->env = env;")
//                        child("this->obj = env->NewGlobalRef(obj);")
                        members.forEach { member ->
                            if (member.type == "jfieldID") {
                                val javaField =
                                    declareMembers.find { it.name + "Field" == member.name }!!
                                val javaSig = "\"${javaField.toJavaSignature()}\""
                                val sourceClass = "clazz"
                                val fieldName = "\"${javaField.name}\""
                                child("${member.name} = env->GetFieldID($sourceClass, $fieldName, $javaSig);")
                            } else if (member.type == "jclass") {
                                child("${member.name} = env->GetObjectClass(obj);")
                            }
                        }
                    }
                }

                val hasArrayField = true

                // generate getters
                generateVulkanGetters(clazz, declareMembers)

                generateVulkanFromObject(clazz, declareMembers, hasArrayField)
            }
        }

    val awakeVulkanCpp = "awake-backend-vulkan/src/main/cpp/vulkan-kotlin"
    FileWriter.writeFile(
        "$awakeVulkanCpp/includes/${clazz.simpleName + "Accessor"}.h",
        cppClassCode.first,
    )
    FileWriter.writeFile(
        "$awakeVulkanCpp/${clazz.simpleName + "Accessor"}.cpp",
        cppClassCode.second,
    )
}

private fun getMergedClassName(clazz: Class<*>): String {
    var className = clazz.simpleName
    if (clazz.kotlin.superclasses.any { it.isSealed }) {
        val parent = clazz.kotlin.superclasses.first()
        className = parent.simpleName + clazz.simpleName
    }
    return className
}

fun getParentClassName(clazz: Class<*>): String {
    var className = clazz.simpleName
    if (clazz.kotlin.superclasses.any { it.isSealed }) {
        val parent = clazz.kotlin.superclasses.first()
        if (clazz.isAnnotationPresent(VkUnionMember::class.java)) {
            val unionMember = clazz.getDeclaredAnnotation(VkUnionMember::class.java)
            if (unionMember != null && unionMember.saveToParent) {
                className = parent.simpleName.toString()
            }
        }
    }
    return className
}

private fun CppClassBuilder.generateVulkanGetters(
    clazz: Class<*>,
    declareMembers: Array<Field>,
) {
    declareMembers.forEach { javaMember ->
        val returnType: String = if (javaMember.type.isEnum) {
            javaMember.type.simpleName
        } else if (javaMember.type.isArray) {
            javaMember.toVulkanType()
        } else {
            javaMember.toVulkanType()
        }
        val functionName = "get" + javaMember.name
        fun getReturnType(type: Class<*>): String = when {
            type.isEnum -> returnType
            type.isPrimitive -> when {
                javaMember.isVkConstArray() -> "void"
                javaMember.isVkPointer() -> "void"
                else -> returnType
            }

            type.isArray -> when {
                type.componentType.isEnum -> "void"
                else -> when (javaMember.getArrayElementJavaType()) {
                    JNIType.JString -> getReturnType(type.componentType)
                    JNIType.JObject -> getReturnType(type.componentType)
                    else -> "void"
                }
            }

            else -> "void"
        }

        fun getParameter(type: Class<*>): List<Pair<String, String>> = when {
            type.isEnum -> emptyList()
            type.isPrimitive -> when {
                javaMember.isVkPointer() -> listOf(
                    Pair(
                        "clazzInfo",
                        "${getParentClassName(clazz)}&",
                    ),
                )

                else -> emptyList()
            }

            type.isArray -> when {
                type.componentType.isEnum -> listOf(
                    Pair(
                        "clazzInfo",
                        "${getParentClassName(clazz)}&",
                    ),
                )

                else -> when (javaMember.getArrayElementJavaType()) {
                    JNIType.JString -> getParameter(type.componentType)
                    JNIType.JObject -> getParameter(type.componentType)
                    else -> listOf(Pair("clazzInfo", "${getParentClassName(clazz)}&"))
                }
            }

            else -> listOf(Pair("clazzInfo", "${getParentClassName(clazz)}&"))
        }

        fun CppFunctionBodyBuilder.createBody() {
            val void = getReturnType(javaMember.type) == "void"
            when {
                javaMember.type.isEnum -> processEnumAccessor(javaMember, returnType)
                javaMember.type.isPrimitive -> processPrimitiveAccessor(javaMember, returnType)
                javaMember.type.isArray -> processArrayAccessor(
                    void,
                    clazz,
                    javaMember,
                    returnType,
                    import = { import(it) },
                )

                else -> processObjectAccessor(void, javaMember, returnType, import = { import(it) })
            }
        }
        function(
            1,
            getReturnType(javaMember.type),
            functionName,
            getParameter(javaMember.type),
        ) {
            body(2) {
                createBody()
            }
        }
    }
}

fun CppFunctionBodyBuilder.processArrayAccessor(
    void: Boolean,
    clazz: Class<*>,
    javaMember: Field,
    returnType: String,
    import: (dependency: String) -> Unit,
) {
    val arrayName = javaMember.name
    val suffix = javaMember.javaTypeSuffix()
    val localVariable = javaMember.name + suffix
    val javaType = javaMember.toJavaTypeArray()
    val javaValue = javaMember.getJavaValue(
        "env",
        "obj",
        javaMember.name + "Field",
    )

    fun processDefaultElement(clazzInfo: String, arrayVariable: String) {
        // generate array size assignee
        val vkArray = javaMember.getVkArray()
        if (vkArray != null) {
            val arraySizeName = vkArray.sizeAlias // javaMember.name + "Count"
            if (arraySizeName.isNotEmpty()) {
                child("    $clazzInfo.$arraySizeName = 0;")
            }
            child("    clazzInfo.$arrayName = nullptr;")
        } else if (javaMember.isVkConstArray()) {
            child("    // const array")
            child("    // clazzInfo.$arrayName = nullptr;")
        } else if (clazz.isAnnotationPresent(VkUnionMember::class.java)) {
            val unionMember = clazz.getDeclaredAnnotation(VkUnionMember::class.java)
            child("    // ${unionMember.alias} no need to release;")
        } else {
            child("    clazzInfo.$arrayName = nullptr;")
        }
        child("    env->DeleteLocalRef($arrayVariable); // release null reference")
    }

    fun processStringElement(javaType: JNIType) {
        child("$returnType $arrayName;")
        child("for (int i = 0; i < size; ++i) {")
        child(
            "     auto element = ($javaType) env->${
                javaMember.getArrayElement(
                    localVariable,
                    "i",
                )
            };",
        )
        child("     auto str = env->GetStringUTFChars(element, nullptr);")
        child("     auto result = strdup(str); // Allocate memory and copy the string")
        child("     env->ReleaseStringUTFChars(element, str); // Release the local string reference")
        child("     $arrayName.push_back(result);")
        child("}")
    }

    fun processObjectElement(javaType: JNIType) {
        val accessor =
            "${javaMember.type.componentType.simpleName}Accessor" // TODO fix hardcoded

        child("$returnType $arrayName;")
        child("for (int i = 0; i < size; ++i) {")
        child(
            "     auto element = ($javaType) env->${
                javaMember.getArrayElement(
                    localVariable,
                    "i",
                )
            };",
        )
        if (returnType !in listOf("void*", "std::vector<void*>")) {
            val kotlinElement = javaMember.type.componentType.kotlin
            if (kotlinElement.isSealed) {
                child("    // sealed class")
                kotlinElement.sealedSubclasses.forEach { subClass ->
                    val subClassName = "${subClass.simpleName?.replaceFirstChar { it.lowercase() }}Class"
                    child(
                        "   auto $subClassName = env->FindClass(\"${
                            subClass.qualifiedName?.replace(
                                ".",
                                "/",
                            )
                        }\");",
                    )
                    child("   if(env->IsInstanceOf(element, $subClassName)) {")
                    val subClassAccessor = subClass.simpleName + "Accessor"
                    import("<includes/$subClassAccessor.h>")
                    child("        $subClassAccessor accessor(env, element);") // TODO fix hardcoded
                    if (void) {
                        child("        ${subClass.simpleName} ref{};")
                        child("        accessor.fromObject(ref);")
                        val unionMember = subClass.findAnnotation<VkUnionMember>()
                        if (unionMember != null) {
                            child("        ${kotlinElement.simpleName} value = {.${unionMember.alias} {ref}};")
                            child("        $arrayName.push_back(value);")
                        } else {
                            child("         // not a union member??")
                            child("        $arrayName.push_back(ref);")
                        }
                    } else {
                        child("        $arrayName.push_back(accessor.fromObject());")
                    }
                    child("        env->DeleteLocalRef($subClassName);")
                    child("        continue;")
                    child("   }")
                }
            } else {
                import("<includes/$accessor.h>")
                child("   // experimental optimize accessor")
                child("    $accessor accessor(env, element);") // TODO fix hardcoded
                if (void) {
                    child("    ${javaMember.type.componentType.simpleName} ref{};")
                    child("    accessor.fromObject(ref);")
                    child("    $arrayName.push_back(ref);")
                } else {
                    child("    $arrayName.push_back(accessor.fromObject());")
                }
            }
        } else {
            // possible type is Any or Null??
            child("    $arrayName.push_back(element); // type is Any??")
        }
        child("    env->DeleteLocalRef(element); // release element reference")
        child("}")
    }

    fun processObjectVkHandleElement() {
        child("// vkhandle array?")
        child("$returnType $arrayName;")
        child("for (int i = 0; i < size; ++i) {")
        val handle = javaMember.getVkHandle()
        child(
            "     auto element = env->GetObjectArrayElement($localVariable, i);",
        )
        // get long value
        child(
            "   jmethodID getValueMethod = ${
                javaMember.type.componentType.getObjectJavaValue(
                    "element",
                    javaMember.toJavaSignature(true),
                )
            };",
        )
        child("   jlong value = env->Call${javaMember.type.componentType.simpleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}Method(element, getValueMethod);")

        child("   $arrayName.push_back(reinterpret_cast<${handle.name}>(value)); //vkhandle ")
        child("   env->DeleteLocalRef(element); // release element reference")
        child("}")
    }

    fun processPrimitiveElement() {
        child("// primitive array?")
        child("$returnType $arrayName(size);")
        child(
            "env->${
                javaMember.getArrayRegion(
                    localVariable,
                    "0",
                    "size",
                    "reinterpret_cast<${javaMember.getArrayElementJavaType()} *>($arrayName.data())",
                )
            }",
        )
    }

    fun processEnumElement() {
        // process enum element
        child("$returnType $arrayName;")
        child("for (int i = 0; i < size; ++i) {")
        child(
            "     auto element = ($javaType) env->${
                javaMember.getArrayElement(
                    localVariable,
                    "i",
                )
            };",
        )
        val enumName = javaMember.type.componentType.simpleName
        child("     $arrayName.push_back(static_cast<$enumName>(enum_utils::getEnumFromObject(env, element))); // type is enum")
        child("}")
    }

    fun processCopy(clazzInfo: String, arrayVariable: String) {
        child("// processing array data")
        val elementType = javaMember.type.componentType
        // generate array size assignee
        val vkArray = javaMember.getVkArray()
        if (vkArray != null) {
            val arraySizeName = vkArray.sizeAlias // javaMember.name + "Count"
            if (arraySizeName.isNotEmpty()) {
                val stride = if (vkArray.stride == UInt::class) {
                    " * sizeof(uint32_t)"
                } else {
                    ""
                }
                child("auto $arraySizeName = static_cast<uint32_t>($arrayName.size()$stride);")
                child("$clazzInfo.$arraySizeName = $arraySizeName;")
            } else {
                child("// no array size generated")
            }
        }
        if (elementType.isPrimitive) {
            if (javaMember.isVkConstArray()) {
                // fixed size array
                child("std::copy($arrayName.begin(), $arrayName.end(), $clazzInfo.$arrayName); // fixed array size")
            } else if (clazz.isAnnotationPresent(VkUnionMember::class.java)) {
                val unionMember = clazz.getDeclaredAnnotation(VkUnionMember::class.java)
                child("std::copy($arrayName.begin(), $arrayName.end(), $clazzInfo.${unionMember.alias}); // union member")
            } else {
                child("// Make a copy of the primitive to ensure proper memory management;")
                val javaTypeArray = javaMember.toJavaTypeArray()
                val newData = if (javaTypeArray == JNIType.JIntArray) {
                    "uint32_t"
                } else if (javaMember.isVkHandle()) {
                    javaMember.toVulkanType(false)
                } else {
                    javaMember.type.componentType.simpleName
                }
                child("auto copy = new $newData[size];")
                child("std::copy($arrayName.begin(), $arrayName.end(), copy);")
                child("clazzInfo.$arrayName = copy;")
//                            child("$clazzInfo.${arrayName} = $arrayName.data(); // primitive $comment")
            }
        } else {
//                        child("$clazzInfo.${arrayName} = $arrayName.data(); // object $comment")
            child("// Make a copy of the object to ensure proper memory management;")
            child("// $javaType")
            val newData = when {
                javaType == JNIType.JString || javaType == JNIType.JStringArray -> "const char*"
                !javaMember.type.componentType.simpleName.startsWith(
                    "vk",
                    true,
                ) &&
                    javaType == JNIType.JObject -> "const void*"

                (javaType == JNIType.JLong || javaType == JNIType.JLongArray) && javaMember.isVkHandle() -> javaMember.getVkHandle().name
                javaType == JNIType.JInt || javaType == JNIType.JIntArray -> "uint32_t"
                javaType == JNIType.JObjectArray && javaMember.isVkHandle() -> javaMember.getVkHandle().name
                javaType == JNIType.JObjectArray && javaMember.type.componentType == String::class.java -> "const char*"
                javaType == JNIType.JObjectArray && javaMember.type.componentType == Any::class.java -> "const void*"
                else -> javaMember.type.componentType.simpleName
            }
            child("auto copy = new $newData[size];")
            child("std::copy($arrayName.begin(), $arrayName.end(), copy);")
            child("clazzInfo.$arrayName = copy;")
        }
        child("env->DeleteLocalRef($arrayVariable); // release reference")
    }

    // create script here

    child("auto $localVariable = ($javaType) $javaValue;")
    child("if($localVariable == nullptr) {")
    processDefaultElement("clazzInfo", localVariable)
    if (void) {
        child("    return;")
    } else {
        child("    return {};")
    }
    child("}")
    child("auto size = env->GetArrayLength($localVariable);")
    val element = javaMember.type.componentType
    val javaElementType = javaMember.getArrayElementJavaType()
    when {
        element.isPrimitive -> if (!javaMember.isVkHandle()) {
            processPrimitiveElement()
        } else {
            processObjectVkHandleElement()
        }

        element.isEnum -> processEnumElement()
        element.isArray -> throw RuntimeException("Nested array not supported")
        else -> when (javaElementType) {
            JNIType.JString -> processStringElement(javaElementType)
            JNIType.JObject -> processObjectElement(javaElementType)
            else -> {
                if (!javaMember.isVkHandle()) {
                    processPrimitiveElement()
                } else {
                    processObjectVkHandleElement()
                }
            }
        }
    }
    processCopy("clazzInfo", localVariable)
}

fun CppFunctionBodyBuilder.processObjectAccessor(
    void: Boolean,
    javaMember: Field,
    returnType: String,
    import: (dependency: String) -> Unit,
) {
    val suffix = javaMember.javaTypeSuffix()
    val localVariable = javaMember.name + suffix
    val javaType = javaMember.toJavaType()
    val javaValue = javaMember.getJavaValue(
        "env",
        "obj",
        javaMember.name + "Field",
    )
    // object
    if (javaType == JNIType.JString) {
        child("auto $localVariable = ($javaType) $javaValue;")
        child("auto str = ($returnType) env->GetStringUTFChars($localVariable, nullptr);")
        child("auto result = strdup(str); // Allocate memory and copy the string")
        child("env->ReleaseStringUTFChars($localVariable, str); // Release the local string reference")
        if (javaMember.isVkConstArray()) {
            val javaConst = javaMember.getVkConstArray()
            child("strncpy(clazzInfo.${javaMember.name}, result, ${javaConst?.arraySize});")
        } else {
            child("clazzInfo.${javaMember.name} = result;")
        }
    } else {
        if (javaMember.isVkHandle()) {
            child("return reinterpret_cast<$returnType>($javaValue); // VkHandle from object??")
        } else if (returnType.startsWith("vk", true)) {
            // vulkan object
            // object is null??
            // should be accessed by an accessor
            val accessor =
                "${javaMember.type.simpleName}Accessor" // TODO fix hardcoded
            import("<includes/$accessor.h>") // TODO move to .h
            child("auto $localVariable = ($javaType) $javaValue;")
            child("if($localVariable == nullptr) {")
            child("    env->DeleteLocalRef($localVariable); // Delete null object reference")
            child("    return;")
            child("}")
            child("$accessor accessor(env, $localVariable);") // TODO fix hardcoded
            // experimental
            if (void) {
                child("${javaMember.type.simpleName} ref{};")
                child("accessor.fromObject(ref);")
                if (javaMember.isVkPointer()) {
                    child("clazzInfo.${javaMember.name} = &ref;")
                } else {
                    child("clazzInfo.${javaMember.name} = ref;")
                }
                child("env->DeleteLocalRef($localVariable); // Delete object reference")
            } else {
                child("return ($returnType) (accessor.fromObject()); // Object is null, should be accessed by an accessor")
            }
        } else {
            if (void) {
                if (!javaMember.isAnnotationPresent(NativeSurfaceWindow::class.java)) {
                    child("auto ref = ($returnType) ($javaType) $javaValue; // Any Object")
                    child("clazzInfo.${javaMember.name} = ref;")
                } else {
                    import("<android/native_window_jni.h>")
                    child("auto windowObj = $javaValue; // Native surface window")
                    child("clazzInfo.${javaMember.name} = ANativeWindow_fromSurface(env, windowObj);")
                }
            } else {
                child("return ($returnType) ($javaType) $javaValue; // Any Object")
            }
        }
    }
}

fun CppFunctionBodyBuilder.processEnumAccessor(javaMember: Field, returnType: String) {
    val suffix = javaMember.javaTypeSuffix()
    val localVariable = javaMember.name + suffix
    val javaType = javaMember.toJavaType()
    val javaValue = javaMember.getJavaValue(
        "env",
        "obj",
        javaMember.name + "Field",
    )
    child("auto $localVariable = ($javaType) $javaValue;")
    child("auto enumValue = ($returnType) enum_utils::getEnumFromObject(env, $localVariable);")
    child("env->DeleteLocalRef($localVariable); // release enum reference")
    child("return enumValue;")
}

fun CppFunctionBodyBuilder.processPrimitiveAccessor(javaMember: Field, returnType: String) {
    val javaType = javaMember.toJavaType()
    val javaValue = javaMember.getJavaValue(
        "env",
        "obj",
        javaMember.name + "Field",
    )
    if (javaMember.isVkHandle()) {
        child("auto value = $javaValue;")
        child("if(value == 0) {")
        child("     return VK_NULL_HANDLE;")
        child("}")
        child("return reinterpret_cast<$returnType>(value); // VkHandle")
    } else if (javaMember.isVkPointer()) {
        child("auto ptr = ($returnType) $javaValue; // Primitive Pointer")
        child("if(ptr != 0) {")
        child("    clazzInfo.${javaMember.name} = &ptr; // Primitive Pointer")
        child("}")
    } else {
        child("return ($returnType) ($javaType) $javaValue; // primitive")
    }
}
