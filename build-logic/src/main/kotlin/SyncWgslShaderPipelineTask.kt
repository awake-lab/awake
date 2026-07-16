import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class SyncWgslShaderPipelineTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val webGpuOutputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val vulkanOutputDirectory: DirectoryProperty

    @get:Input
    abstract val nagaExecutable: Property<String>

    @get:Input
    abstract val vertexEntryPoint: Property<String>

    @get:Input
    abstract val fragmentEntryPoint: Property<String>

    @TaskAction
    fun sync() {
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

        val webGpuRoot = webGpuOutputDirectory.asFile.get().also(File::mkdirs)
        val vulkanRoot = vulkanOutputDirectory.asFile.get().also(File::mkdirs)
        val expectedWebGpu = mutableSetOf<String>()
        val expectedVulkan = mutableSetOf<String>()

        wgslFiles.forEach { sourceFile ->
            val relativePath = sourceFile.relativeTo(sourceRoot).invariantSeparatorsPath
            val outputName = sourceFile.nameWithoutExtension

            val webGpuTarget = File(webGpuRoot, relativePath)
            webGpuTarget.parentFile.mkdirs()
            sourceFile.copyTo(webGpuTarget, overwrite = true)
            expectedWebGpu += webGpuTarget.canonicalPath

            val relativeParent = sourceFile.relativeTo(sourceRoot).parentFile?.invariantSeparatorsPath
            val vulkanParent = relativeParent?.let { File(vulkanRoot, it) } ?: vulkanRoot
            vulkanParent.mkdirs()
            val vertexOutput = File(vulkanParent, "$outputName.vert.spv")
            val fragmentOutput = File(vulkanParent, "$outputName.frag.spv")

            runNaga(
                input = sourceFile,
                output = vertexOutput,
                entryPoint = vertexEntryPoint.get(),
                stage = "vert"
            )
            runNaga(
                input = sourceFile,
                output = fragmentOutput,
                entryPoint = fragmentEntryPoint.get(),
                stage = "frag"
            )

            expectedVulkan += vertexOutput.canonicalPath
            expectedVulkan += fragmentOutput.canonicalPath
        }

        pruneStaleOutputs(webGpuRoot, expectedWebGpu, "wgsl")
        pruneStaleOutputs(vulkanRoot, expectedVulkan, "spv")
    }

    private fun pruneStaleOutputs(root: File, expectedPaths: Set<String>, extension: String) {
        if (!root.exists()) return
        root.walkTopDown()
            .filter { it.isFile && it.extension == extension && it.canonicalPath !in expectedPaths }
            .forEach(File::delete)
    }

    private fun runNaga(
        input: File,
        output: File,
        entryPoint: String,
        stage: String
    ) {
        val executable = nagaExecutable.get()
        val process = try {
            ProcessBuilder(
                executable,
                input.absolutePath,
                output.absolutePath,
                "--input-kind", "wgsl",
                "--keep-coordinate-space",
                "--entry-point", entryPoint,
                "--shader-stage", stage
            )
                .directory(project.projectDir)
                .redirectErrorStream(true)
                .start()
        } catch (error: Exception) {
            throw GradleException(
                "Unable to launch `$executable` while syncing shaders. " +
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
                    appendLine("naga failed for ${input.invariantSeparatorsPath} [$stage:$entryPoint].")
                    appendLine("Output file: ${output.invariantSeparatorsPath}")
                    append(outputText.ifBlank { "No compiler output." })
                }
            )
        }
    }
}
