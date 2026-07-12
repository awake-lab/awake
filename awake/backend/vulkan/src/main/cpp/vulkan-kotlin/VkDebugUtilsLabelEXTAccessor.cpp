/*
 *  VkDebugUtilsLabelEXTAccessor.cpp
 *  Vulkan accessor for VkDebugUtilsLabelEXT
 *  Created by Ron June Valdoz */

#include <includes/VkDebugUtilsLabelEXTAccessor.h>

VkDebugUtilsLabelEXTAccessor::VkDebugUtilsLabelEXTAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                       obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    pLabelNameField = env->GetFieldID(clazz, "pLabelName", "Ljava/lang/String;");
    colorField = env->GetFieldID(clazz, "color", "[F");
}

VkStructureType
VkDebugUtilsLabelEXTAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkDebugUtilsLabelEXTAccessor::fromObject(VkDebugUtilsLabelEXT &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    getpLabelName(clazzInfo); // Other const char*
    getcolor(clazzInfo);  // float Object Array
}

void
VkDebugUtilsLabelEXTAccessor::getpLabelName(VkDebugUtilsLabelEXT &clazzInfo) {
    auto pLabelNameObj = (jstring) env->GetObjectField(obj, pLabelNameField);
    auto str = (const char *) env->GetStringUTFChars(pLabelNameObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(pLabelNameObj, str); // Release the local string reference
    clazzInfo.pLabelName = result;
}

void
VkDebugUtilsLabelEXTAccessor::getcolor(VkDebugUtilsLabelEXT &clazzInfo) {
    auto colorArray = (jfloatArray) env->GetObjectField(obj, colorField);
    if (colorArray == nullptr) {
        // const array
        // clazzInfo.color = nullptr;
        env->DeleteLocalRef(colorArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(colorArray);
    // primitive array?
    std::vector<float> color(size);
    env->GetFloatArrayRegion(colorArray, 0, size, reinterpret_cast<jfloat *>(color.data()));
    // processing array data
    std::copy(color.begin(), color.end(), clazzInfo.color); // fixed array size
    env->DeleteLocalRef(colorArray); // release reference
}

void
VkDebugUtilsLabelEXTAccessor::getpNext(VkDebugUtilsLabelEXT &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

VkDebugUtilsLabelEXTAccessor::~VkDebugUtilsLabelEXTAccessor() {
    env->DeleteLocalRef(clazz);
}

