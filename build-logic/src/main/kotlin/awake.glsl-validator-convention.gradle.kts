import de.undercouch.gradle.tasks.download.Download
import java.io.File

plugins {
    id("de.undercouch.download")
}

val glslangDownload =
    tasks.register<Download>("glslangDownload") {
        val osName = System.getProperty("os.name").lowercase()
        val hostFile = when {
            osName.contains("mac") -> "main-osx"
            osName.contains("win") -> "master-windows"
            osName.contains("linux") -> "main-linux"
            else -> throw Exception("$osName not supported")
        }
        src("https://github.com/KhronosGroup/glslang/releases/download/main-tot/glslang-$hostFile-Release.zip")
        dest(layout.buildDirectory.file("glslang.zip"))
    }

val glslangDownloadCopy = tasks.register<Copy>("glslangDownloadCopy") {
    dependsOn(glslangDownload)
    from(zipTree(layout.buildDirectory.file("glslang.zip")))
    into(layout.buildDirectory.dir("glslang"))
}

tasks.register("glslValidator") {
    dependsOn(glslangDownloadCopy)

    val bin = layout.buildDirectory.dir("glslang/bin").get().asFile.path
    val shadersDir = file("src/commonMain/resources/assets/shader/vulkan")
    val shaders = project.fileTree(shadersDir) {
        include("**/*.frag", "**/*.vert")
    }

    doLast {
        shaders.forEach { shaderFile ->
            val spvFile = File(shadersDir, "${shaderFile.name}.spv")
            val process = ProcessBuilder(
                "$bin/glslangValidator", "-V", shaderFile.absolutePath, "-o", spvFile.absolutePath
            ).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw RuntimeException("glslangValidator failed for ${shaderFile.name} (exit $exitCode):\n$output")
            }
        }
    }
}
