//
// Created by Ron June Valdoz on 8/1/23.
//

#include "exception_utils.h"
#include "enum_utils.h"

namespace exception_utils {
    void resultException(JNIEnv *env, jint result, const char *message) {
        jclass exceptionClass = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/utils/VkResultException");
        if (exceptionClass != nullptr) {
            jmethodID constructor = env->GetMethodID(exceptionClass, "<init>",
                                                     "(Ljava/lang/String;Lio/github/ronjunevaldoz/awake/vulkan/enums/VkResult;)V");
            if (constructor != nullptr) {
                jstring jMessage = env->NewStringUTF(message);
                auto resultEnum = enum_utils::setEnumFromVulkan(env, result,
                                                                "io/github/ronjunevaldoz/awake/vulkan/enums/VkResult");
                jobject exceptionObj = env->NewObject(exceptionClass, constructor, jMessage,
                                                      resultEnum);
                env->Throw(reinterpret_cast<jthrowable>(exceptionObj));
                env->DeleteLocalRef(jMessage);
                env->DeleteLocalRef(exceptionClass);
                env->DeleteLocalRef(resultEnum);
            }
        }
    }
} // exception_utils