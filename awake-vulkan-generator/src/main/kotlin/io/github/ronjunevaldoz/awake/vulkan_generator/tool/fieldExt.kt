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

package io.github.ronjunevaldoz.awake.vulkan_generator.tool

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkConstArray
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.VkPointer
import java.lang.reflect.Field

enum class JNIType(val value: String) {
    JByte("jbyte"),
    JChar("jchar"),
    JBoolean("jboolean"),
    JShort("jshort"),
    JInt("jint"),
    JLong("jlong"),
    JDouble("jdouble"),
    JFloat("jfloat"),
    JString("jstring"),
    JObject("jobject"),

    JByteArray("jbyteArray"),
    JCharArray("jcharArray"),
    JBooleanArray("jbooleanArray"),
    JShortArray("jshortArray"),
    JIntArray("jintArray"),
    JLongArray("jlongArray"),
    JDoubleArray("jdoubleArray"),
    JFloatArray("jfloatArray"),
    JStringArray("jstringArray"),
    JObjectArray("jobjectArray")
    ;

    override fun toString(): String {
        return value
    }
}

fun Field.javaTypeSuffix() = when {
    type.isPrimitive -> type.simpleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    type.isEnum -> "Enum"
    type.isArray -> "Array"
    else -> "Obj"
}

fun Field.primitiveTypeIsNull(): Boolean {
    // when a primitive become null, the value of isPrimitive became false
    return !type.isPrimitive &&
            (type.simpleName.contains("int", true) ||
                    type.simpleName.contains("long", true) ||
                    type.simpleName.contains("short", true) ||
                    type.simpleName.contains("byte", true) ||
                    type.simpleName.contains("double", true) ||
                    type.simpleName.contains("float", true))
}

fun Class<*>.toVulkanType(): String {
    val isPrimitiveArray = isArray && componentType.isPrimitive
    val isArray = isArray && !isPrimitiveArray
    val elementType = if (isArray) componentType.simpleName else simpleName
    val simpleName = elementType.lowercase()

    if (isAnnotationPresent(VkHandleRef::class.java)) {
        val handle = getDeclaredAnnotation(VkHandleRef::class.java)
        return handle.name
    }

    // by default the value should all unsigned
    var prefix = "u"

    if (name.contains("index", true)) {
        prefix = ""
    }

    val type = when {
        simpleName.contains("int") -> "${prefix}int32_t"
        simpleName.contains("long") -> "${prefix}int64_t"
        simpleName.contains("short") -> "${prefix}int16_t"
        simpleName.contains("byte") -> "${prefix}int8_t"
        simpleName.contains("double") -> "double"
        simpleName.contains("float") -> "float"
        simpleName.contains("boolean") -> "VkBool32"
        simpleName.contains("char") -> "char"
        simpleName.contains("string") -> "const char*"
        simpleName.contains("object") -> "void*"
        isArray -> elementType
        else -> elementType
    }

    return type
}

fun Field.toVulkanType(useVector: Boolean = true): String {
    val isPrimitiveArray = type.isArray && type.componentType.isPrimitive
    val isArray = type.isArray && !isPrimitiveArray
    val elementType = if (isArray) type.componentType.simpleName else type.simpleName
    val simpleName = elementType.lowercase()
    if (isAnnotationPresent(VkHandleRef::class.java)) {
        val handle = getDeclaredAnnotation(VkHandleRef::class.java)
        if (type.isArray && useVector) {
            return "std::vector<${handle.name}>"
        }
        return handle.name
    }
    val type = type.toVulkanType()
    return if (isPrimitiveArray) {
        "std::vector<${type}>"
    } else if (isArray) {
        return if (this.isVkHandle()) {
            "std::vector<${this.getVkHandle().name}>"
        } else if (simpleName.startsWith("vk")) {
            "std::vector<$elementType>"
        } else {
            "std::vector<$type>"
        }
    } else {
        return if (this.isVkHandle()) {
            this.getVkHandle().name
        } else {
            type
        }
    }
}

fun Field.toJavaTypeArray(): JNIType {
    if (isVkHandle()) {
        return JNIType.JObjectArray
    }
    return type.toJavaType(true)
}

fun Field.getArrayElementJavaType(): JNIType {
    return type.componentType.toJavaType()
}

fun Field.toJavaType(): JNIType {
    return type.toJavaType()
}

fun Class<*>.toJavaType(includeArray: Boolean = false): JNIType {
    if (isArray && includeArray) {
        return when (componentType) {
            Int::class.javaPrimitiveType, java.lang.Integer::class.java -> JNIType.JIntArray
            Long::class.javaPrimitiveType, java.lang.Long::class.java -> JNIType.JLongArray
            Short::class.javaPrimitiveType, java.lang.Short::class.java -> JNIType.JShortArray
            Byte::class.javaPrimitiveType, java.lang.Byte::class.java -> JNIType.JByteArray
            Double::class.javaPrimitiveType, java.lang.Double::class.java -> JNIType.JDoubleArray
            Float::class.javaPrimitiveType, java.lang.Float::class.java -> JNIType.JFloatArray
            Char::class.javaPrimitiveType, Char::class.java -> JNIType.JCharArray
            String::class.java, java.lang.String::class.java -> JNIType.JObjectArray
            Object::class.java, java.lang.Object::class.java -> JNIType.JObjectArray
            Boolean::class.javaPrimitiveType, java.lang.Boolean::class.java -> JNIType.JBooleanArray
            else -> JNIType.JObjectArray
        }
    }
    return when (this) {
        Int::class.javaPrimitiveType, Integer::class.java -> JNIType.JInt
        Long::class.javaPrimitiveType, java.lang.Long::class.java -> JNIType.JLong
        Short::class.javaPrimitiveType, java.lang.Short::class.java -> JNIType.JShort
        Byte::class.javaPrimitiveType, java.lang.Byte::class.java -> JNIType.JByte
        Double::class.javaPrimitiveType, java.lang.Double::class.java -> JNIType.JDouble
        Float::class.javaPrimitiveType, java.lang.Float::class.java -> JNIType.JFloat
        Char::class.javaPrimitiveType, Character::class.java -> JNIType.JChar
        String::class.java -> JNIType.JString
        Boolean::class.javaPrimitiveType, java.lang.Boolean::class.java -> JNIType.JBoolean
        else -> JNIType.JObject
    }
}

fun Class<*>?.toTypeName(): String {
    val typeMapping = mapOf(
        Int::class.javaPrimitiveType to "Int",
        Long::class.javaPrimitiveType to "Long",
        Short::class.javaPrimitiveType to "Short",
        Byte::class.javaPrimitiveType to "Byte",
        Double::class.javaPrimitiveType to "Double",
        Float::class.javaPrimitiveType to "Float",
        Char::class.javaPrimitiveType to "Char",
        String::class.java to "Object",
        Boolean::class.javaPrimitiveType to "Boolean"
    )

    return typeMapping[this] ?: "Object"
}

fun Field.getJavaValue(env: String = "env", obj: String, fieldID: String): String {
    val clazz = type
    val type = clazz.toTypeName()
    if (clazz.isArray) {
        return "$env->GetObjectField($obj, $fieldID)"
    }
    return "$env->Get${type}Field($obj, $fieldID)"
}

fun Field.setJavaValue(env: String = "env", obj: String, fieldID: String, value: String): String {
    val type = type.toTypeName()
    return "$env->Set${type}Field($obj, $fieldID, $value)"
}

fun Class<*>.getObjectJavaValue(obj: String, sig: String): String {
    val comType = this
    val type = simpleName
    if (comType.isArray) {
        throw Exception("Not yet supported")
    }
    return "env->GetMethodID(env->GetObjectClass($obj), \"${type.lowercase()}Value\", \"()$sig\")"
}

fun Field.getArrayElement(list: String, index: String): String {
    return when (type.componentType) {
        String::class.javaPrimitiveType, java.lang.String::class.java -> "GetObjectArrayElement($list, $index); // actual type is ${type.simpleName}"
        Object::class.javaPrimitiveType, java.lang.Object::class.java -> "GetObjectArrayElement($list, $index); // actual type is ${type.simpleName}"
        else -> {
            if (type.simpleName.startsWith("vk", true)) {
                "GetObjectArrayElement($list, $index); // actual type is ${type.simpleName}"
            } else {
                throw RuntimeException("getArrayElement not supported (use getArrayRegion instead) ${type.simpleName}")
            }
        }
    }
}

fun Field.setArrayRegion(
    list: String,
    index: String,
    size: String = "1",
    outElement: String
): String {
    val prefix = "Set"
    val arrayRegionConstructor = "($list, $index, $size, $outElement);"
    return when (type.componentType) {
        Int::class.javaPrimitiveType, Integer::class.java -> "${prefix}IntArrayRegion$arrayRegionConstructor"
        Long::class.javaPrimitiveType, java.lang.Long::class.java -> "${prefix}LongArrayRegion$arrayRegionConstructor"
        Short::class.javaPrimitiveType, java.lang.Short::class.java -> "${prefix}ShortArrayRegion$arrayRegionConstructor"
        Byte::class.javaPrimitiveType, java.lang.Byte::class.java -> "${prefix}ByteArrayRegion$arrayRegionConstructor"
        Double::class.javaPrimitiveType, java.lang.Double::class.java -> "${prefix}DoubleArrayRegion$arrayRegionConstructor"
        Float::class.javaPrimitiveType, java.lang.Float::class.java -> "${prefix}FloatArrayRegion$arrayRegionConstructor"
        Char::class.javaPrimitiveType, java.lang.Character::class.java -> "${prefix}CharArrayRegion$arrayRegionConstructor"
        Boolean::class.javaPrimitiveType, java.lang.Boolean::class.java -> "${prefix}BooleanArrayRegion$arrayRegionConstructor"
//        String::class.javaPrimitiveType, java.lang.String::class.java -> "GetObjectArrayElement($list, $index); // actual type is ${type.simpleName}"
//        Object::class.javaPrimitiveType, java.lang.Object::class.java -> "GetObjectArrayElement($list, $index); // actual type is ${type.simpleName}"
        else -> {
//            if(type.simpleName.startsWith("vk", true)) {
//                "GetObjectArrayElement($list, $index); // generated from Field.getArrayRegion"
//            } else {
            throw RuntimeException("getArrayRegion not supported ${type.simpleName}")
//            }
        }
    }
}

fun Field.getArrayRegion(
    list: String,
    index: String,
    size: String = "1",
    outElement: String
): String {
    val arrayRegionConstructor = "($list, $index, $size, $outElement);"
    return when (type.componentType) {
        Int::class.javaPrimitiveType, Integer::class.java -> "GetIntArrayRegion$arrayRegionConstructor"
        Long::class.javaPrimitiveType, java.lang.Long::class.java -> "GetLongArrayRegion$arrayRegionConstructor"
        Short::class.javaPrimitiveType, java.lang.Short::class.java -> "GetShortArrayRegion$arrayRegionConstructor"
        Byte::class.javaPrimitiveType, java.lang.Byte::class.java -> "GetByteArrayRegion$arrayRegionConstructor"
        Double::class.javaPrimitiveType, java.lang.Double::class.java -> "GetDoubleArrayRegion$arrayRegionConstructor"
        Float::class.javaPrimitiveType, java.lang.Float::class.java -> "GetFloatArrayRegion$arrayRegionConstructor"
        Char::class.javaPrimitiveType, java.lang.Character::class.java -> "GetCharArrayRegion$arrayRegionConstructor"
        Boolean::class.javaPrimitiveType, java.lang.Boolean::class.java -> "GetBooleanArrayRegion$arrayRegionConstructor"
//        String::class.javaPrimitiveType, java.lang.String::class.java -> "GetObjectArrayElement($list, $index); // actual type is ${type.simpleName}"
//        Object::class.javaPrimitiveType, java.lang.Object::class.java -> "GetObjectArrayElement($list, $index); // actual type is ${type.simpleName}"
        else -> {
//            if(type.simpleName.startsWith("vk", true)) {
//                "GetObjectArrayElement($list, $index); // generated from Field.getArrayRegion"
//            } else {
            throw RuntimeException("getArrayRegion not supported ${type.simpleName}")
//            }
        }
    }
}

fun Field.toJavaSignature(element: Boolean = false): String {
    return when (val javaTypeName =
        if (element) type.componentType.simpleName.lowercase() else type.name) {
        "boolean" -> "Z"
        "byte" -> "B"
        "char" -> "C"
        "short" -> "S"
        "int" -> "I"
        "long" -> "J"
        "float" -> "F"
        "double" -> "D"
        "void" -> "V"
        else -> {
            if (javaTypeName.startsWith("[")) {
                // Array types
                javaTypeName
            } else if (javaTypeName.startsWith("L") && javaTypeName.endsWith(";")) {
                // Object types
                "L" + javaTypeName.substring(1, javaTypeName.length - 1) + ";"
            } else {
                "L$javaTypeName;"
            }
        }
    }.replace(".", "/")
}


// vulkan extensions

fun Field.isVkHandle(): Boolean {
//    val isVkHandle = kotlinProperty!!.returnType.classifier == VkHandle::class
    if (isAnnotationPresent(VkHandleRef::class.java)) {
        return true
    }
    return false
}

fun Field.getVkHandle(): VkHandleRef {
    return getDeclaredAnnotation(VkHandleRef::class.java)
}

fun Field.isVkPointer(): Boolean {
    if (isAnnotationPresent(VkPointer::class.java)) {
        return true
    }
    return false
}

fun Field.isVkConstArray(): Boolean {
    if (isAnnotationPresent(VkConstArray::class.java)) {
        return true
    }
    return false
}


fun Field.getVkConstArray(): VkConstArray? {
    if (isAnnotationPresent(VkConstArray::class.java)) {
        return getDeclaredAnnotation(VkConstArray::class.java)
    }
    return null
}

fun Field.getVkArray(): VkArray? {
    if (isAnnotationPresent(VkArray::class.java)) {
        return getDeclaredAnnotation(VkArray::class.java)
    }
    return null
}

fun Field.cast(value: String): String {
    val type = toJavaType()
    return if (this.type.isEnum) {
        "enum_utils::setEnumFromVulkan(env, static_cast<jint>(source.${name}), \"${
            this.type.name.replace(
                ".",
                "/"
            )
        }\")"
    } else {
        "static_cast<$type>($value)"
    }
}