//
// Created by Ron June Valdoz on 8/1/23.
//

#ifndef AWAKE_ENUM_UTILS_H
#define AWAKE_ENUM_UTILS_H

#include <jni.h>
#include <string>

namespace enum_utils {

    jint getEnumFromObject(JNIEnv *env, jobject obj);

    jobject setEnumFromVulkan(JNIEnv *env, jint value, const std::string &sig);

} // enum_utils

#endif //AWAKE_ENUM_UTILS_H
