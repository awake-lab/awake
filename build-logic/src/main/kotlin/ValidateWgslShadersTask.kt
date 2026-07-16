import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ValidateWgslShadersTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:Input
    abstract val nagaExecutable: Property<String>

    @TaskAction
    fun validate() {
        val sourceRoot = sourceDirectory.asFile.get()
        if (!sourceRoot.exists()) {
            logger.lifecycle("No WGSL shader sources found at ${sourceRoot.invariantSeparatorsPath}; skipping.")
            return
        }

        val wgslFiles = sourceRoot
            .walkTopDown()
            .filter { it.isFile && it.extension == "wgsl" }
            .sortedBy { it.invariantSeparatorsPath }
            .toList()

        if (wgslFiles.isEmpty()) {
            logger.lifecycle("No WGSL shader sources found at ${sourceRoot.invariantSeparatorsPath}; skipping.")
            return
        }

        wgslFiles.forEach(::runNagaValidation)
    }

    private fun runNagaValidation(input: File) {
        val executable = nagaExecutable.get()
        val process = try {
            ProcessBuilder(
                executable,
                input.absolutePath
            )
                .directory(project.projectDir)
                .redirectErrorStream(true)
                .start()
        } catch (error: Exception) {
            throw GradleException(
                "Unable to launch `$executable` while validating shaders. " +
                    "Install `naga-cli` and make sure `naga` is on PATH, or set " +
                    "`-Pawake.shader.nagaBinary=/absolute/path/to/naga`.",
                error
            )
        }

        val outputText = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException(
                buildString {
                    appendLine("naga validation failed for ${input.invariantSeparatorsPath}.")
                    append(outputText.ifBlank { "No compiler output." })
                }
            )
        }
    }
}
