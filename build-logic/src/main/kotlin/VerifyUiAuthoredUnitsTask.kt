import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyUiAuthoredUnitsTask : DefaultTask() {

    @get:Input
    abstract val modulePath: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val authoredPixelLiteral = Regex("""(?<![A-Za-z0-9_])\d+(?:\.\d+)?f?\.px\b""")
        val violations = mutableListOf<String>()

        sourceFiles.files
            .filter { it.isFile }
            .sortedBy { it.invariantSeparatorsPath }
            .forEach { file ->
                val code = stripComments(file.readText())
                authoredPixelLiteral.findAll(code).forEach { match ->
                    val line = lineNumber(code, match.range.first)
                    violations += "${file.relativeTo(project.projectDir).invariantSeparatorsPath}:$line " +
                        "uses authored pixel literal `${match.value}` in ${modulePath.get()}"
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Authored UI unit violations found in ${modulePath.get()}:")
                    violations.forEach { appendLine("- $it") }
                    append(
                        "Use Dp/Sp for authored UI values. Keep raw pixel literals in low-level " +
                            "layout/render internals only. See docs/reference/ui-ownership.md."
                    )
                }
            )
        }
    }

    private fun stripComments(source: String): String {
        val withoutBlockComments = source.replace(Regex("""(?s)/\*.*?\*/"""), " ")
        return withoutBlockComments
            .lineSequence()
            .joinToString("\n") { line -> line.substringBefore("//") }
    }

    private fun lineNumber(source: String, index: Int): Int = source
        .substring(0, index.coerceIn(0, source.length))
        .count { it == '\n' } + 1
}
