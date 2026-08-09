//
// Created by Ron June Valdoz on 8/1/23.
//

#ifndef AWAKE_EXCEPTION_UTILS_H
#define AWAKE_EXCEPTION_UTILS_H

#include <jni.h>
#include <string>
#include "enum_utils.h"

namespace exception_utils {
    void resultException(JNIEnv *env, jint value, const char *message);

} // enum_utils

#endif //AWAKE_EXCEPTION_UTILS_H
