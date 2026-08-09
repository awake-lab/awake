//
// Created by Ron June Valdoz on 8/1/23.
//

#include "enum_utils.h"

namespace enum_utils {
    jint getEnumFromObject(JNIEnv *env, jobject obj) {
        // Get the class of the object (enum)
        jclass enumClass = env->GetObjectClass(obj);
        if (enumClass == nullptr) {
            // Handle the error if the class retrieval fails
            // For example, log an error message or throw an exception
            return 0; // Return a default value or an appropriate error code
        }

        // Get the method ID of the enum's 'getValue' method
        jmethodID valueMethod = env->GetMethodID(enumClass, "getValue", "()I");
        if (valueMethod == nullptr) {
            // Handle the error if the method retrieval fails
            // For example, log an error message or throw an exception
            env->DeleteLocalRef(enumClass); // Release the local reference
            return 0; // Return a default value or an appropriate error code
        }

        // Call the 'getValue' method on the enum object
        jint value = env->CallIntMethod(obj, valueMethod);

        // Check for exceptions after the method call
        if (env->ExceptionCheck()) {
            // Handle the exception if any occurred
            // For example, log an error message or throw an exception
            env->DeleteLocalRef(enumClass); // Release the local reference
            return 0; // Return a default value or an appropriate error code
        }

        // Release the local reference to the enum class
        env->DeleteLocalRef(enumClass);

        return value;
    }


    jobject setEnumFromVulkan(JNIEnv *env, jint value, const std::string &sig) {
        // Get the class reference for your Java enum class
        jclass enumClass = env->FindClass(sig.data());
        if (enumClass == nullptr) {
            // Handle the error if the class retrieval fails
            // For example, log an error message or throw an exception
            return nullptr; // Return an appropriate value based on your error handling strategy
        }

        // Get the method ID of the enum's 'valueOf' method
        std::string valuesSig = "()[L" + std::string(sig) + ";";
        jmethodID valuesMethod = env->GetStaticMethodID(enumClass, "values", valuesSig.data());

        if (valuesMethod == nullptr) {
            // Handle the error if the method retrieval fails
            // For example, log an error message or throw an exception
            env->DeleteLocalRef(enumClass); // Release the local reference
            return nullptr; // Return an appropriate value based on your error handling strategy
        }
        auto enumValues = (jobjectArray) env->CallStaticObjectMethod(enumClass, valuesMethod);

        jsize numEnumValues = env->GetArrayLength(enumValues);
        for (jsize i = 0; i < numEnumValues; ++i) {
            jobject enumValue = env->GetObjectArrayElement(enumValues, i);
            jmethodID valueMethod = env->GetMethodID(enumClass, "getValue", "()I");
            jint enumValueInt = env->CallIntMethod(enumValue, valueMethod);
            env->DeleteLocalRef(enumValue);

            if (enumValueInt == value) {
                return env->GetObjectArrayElement(enumValues, i);
            }
        }
        return nullptr;
    }
} // enum_utils