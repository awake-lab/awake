// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.tool

import io.github.ronjunevaldoz.awake.vulkan_generator.tool.FileWriter.rootDir
import java.io.File

object FileWriter {
    var rootDir: String? = null
    fun writeFile(fileName: String, content: String) {
        val file = File(rootDir, fileName)

        // Ensure that the directory exists
        file.parentFile.mkdirs()
        try {
            file.createNewFile()
            file.writeText(content)
            println("File created successfully. $fileName")
        } catch (e: Exception) {
            println("Error creating the file: ${e.message} | ${file.absoluteFile}")
        }
    }
}

fun cmakeListTemplate(path: String = ""): String {
    val commonUtilsDir = File(rootDir, path)
    val cmakeListsTxt = buildString {
        append("# vulkan-kotlin/CMakeLists.txt\n\n")
        // Set the source files for the common Vulkan utilities
        append("set(VULKAN_KOTLIN_SOURCES\n")
        commonUtilsDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "cpp") {
                append("        ${file.name}\n")
            }
        }
        append("        )\n\n")

        append("# Set the path to your header file directory\n")
        append("include_directories(includes)\n")
        // Create a static library for the common utilities
        append("add_library(vulkan_kotlin STATIC \${VULKAN_KOTLIN_SOURCES})\n\n")

        // Add the 'include' directory for the library
        append("target_include_directories(vulkan_kotlin PUBLIC \${CMAKE_CURRENT_SOURCE_DIR})\n")
    }
    FileWriter.writeFile("${path}CMakeLists.txt", cmakeListsTxt)
    return cmakeListsTxt
}
