import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ValidateWgslShadersTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    /** See [SyncWgslShaderPipelineTask]'s own doc comment on its identically-named property --
     * same extra-roots/duplicate-filename-throws behavior, shared via [collectWgslFiles]. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val additionalSourceDirectories: ConfigurableFileCollection

    @get:Input
    abstract val nagaExecutable: Property<String>

    @TaskAction
    fun validate() {
        val sourceRoot = sourceDirectory.asFile.get()
        val wgslFiles = collectWgslFiles(sourceRoot, additionalSourceDirectories.files)
        if (wgslFiles.isEmpty()) {
            logger.lifecycle("No WGSL shader sources found at ${sourceRoot.invariantSeparatorsPath}; skipping.")
            return
        }

        wgslFiles.forEach { (file, _) -> runNagaValidation(file) }
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
