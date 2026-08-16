import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Rejects hand-written frame lifecycle code unless the test declares why it needs it. */
abstract class VerifyUiTestLifecycleTask : DefaultTask() {

    @get:Input
    abstract val modulePath: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val lifecycleCall = Regex("""\b(?:beginFrame|endFrame|finishFrame)\s*\(""")
        val lowLevelMarker = Regex("""@(?:file:)?(?:[A-Za-z0-9_.]+\.)?UiLowLevelTest\s*\(""")
        val violations = sourceFiles.files
            .filter { it.isFile }
            .sortedBy { it.invariantSeparatorsPath }
            .mapNotNull { file ->
                val code = stripComments(file.readText())
                if (lifecycleCall.containsMatchIn(code) && !lowLevelMarker.containsMatchIn(code)) {
                    "${file.relativeTo(project.projectDir).invariantSeparatorsPath} uses manual UI frame lifecycle"
                } else {
                    null
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("UI test lifecycle violations found in ${modulePath.get()}:")
                    violations.forEach { appendLine("- $it") }
                    append(
                        "Use renderUiComponent for one frame or uiTestSession for multi-frame input. " +
                            "If lifecycle mechanics are the actual subject, mark the class with " +
                            "@UiLowLevelTest(\"why\"). See docs/reference/ui-validation.md.",
                    )
                },
            )
        }
    }

    private fun stripComments(source: String): String {
        val withoutBlockComments = source.replace(Regex("""(?s)/\*.*?\*/"""), " ")
        return withoutBlockComments.lineSequence().joinToString("\n") { it.substringBefore("//") }
    }
}
