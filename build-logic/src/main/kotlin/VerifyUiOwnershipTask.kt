import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyUiOwnershipTask : DefaultTask() {

    @get:Input
    abstract val modulePath: Property<String>

    @get:Input
    abstract val forbiddenDeclarationNames: ListProperty<String>

    @get:Input
    abstract val forbiddenTypeReferences: ListProperty<String>

    /**
     * Source-level patterns for boundaries Kotlin visibility cannot express.  For example, a
     * downstream module can legally see a public [UiScope] member even when using it violates
     * the UI layering policy.
     */
    @get:Input
    abstract val forbiddenSourcePatterns: ListProperty<String>

    /**
     * Path suffixes (invariant separators) of files exempt from [forbiddenSourcePatterns]
     * only — a tracked-debt ledger, not a general opt-out. Declaration and type-reference
     * rules still apply to exempt files.
     */
    @get:Input
    abstract val exemptSourcePatternFiles: ListProperty<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val forbiddenDeclarations = forbiddenDeclarationNames.get().toSet()
        val forbiddenReferences = forbiddenTypeReferences.get().toSet()
        val forbiddenPatterns = forbiddenSourcePatterns.get()
        val exemptPatternFiles = exemptSourcePatternFiles.get()
        val violations = mutableListOf<String>()

        sourceFiles.files
            .filter { it.isFile }
            .sortedBy { it.invariantSeparatorsPath }
            .forEach { file ->
                val code = stripComments(file.readText())
                extractDeclarations(code).forEach { declaration ->
                    if (declaration.name in forbiddenDeclarations) {
                        violations += "${file.relativeTo(project.projectDir).invariantSeparatorsPath}:${declaration.line} " +
                            "declares forbidden helper-shaped API `${declaration.name}` in ${modulePath.get()}"
                    }
                }
                forbiddenReferences.forEach { reference ->
                    val match = Regex("""\b${Regex.escape(reference)}\b""").find(code) ?: return@forEach
                    val line = lineNumber(code, match.range.first)
                    violations += "${file.relativeTo(project.projectDir).invariantSeparatorsPath}:$line " +
                        "references runtime/sample-bound symbol `$reference` in ${modulePath.get()}"
                }
                val relativePath = file.relativeTo(project.projectDir).invariantSeparatorsPath
                if (exemptPatternFiles.none { relativePath.endsWith(it) }) {
                    forbiddenPatterns.forEach { pattern ->
                        Regex(pattern).findAll(code).forEach { match ->
                            val line = lineNumber(code, match.range.first)
                            violations += "$relativePath:$line " +
                                "uses forbidden UI boundary escape hatch `${match.value}` in ${modulePath.get()}"
                        }
                    }
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("UI ownership violations found in ${modulePath.get()}:")
                    violations.forEach { appendLine("- $it") }
                    append("See docs/reference/ui-ownership.md and docs/reference/game-structure.md.")
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

    private fun extractDeclarations(source: String): List<Declaration> {
        val patterns = listOf(
            Regex("""\bfun\s+(?:<[^>]+>\s*)?(?:[A-Za-z0-9_?.]+\.)?([A-Za-z_][A-Za-z0-9_]*)\b"""),
            Regex("""\b(?:data|enum|sealed)\s+class\s+([A-Za-z_][A-Za-z0-9_]*)\b"""),
            Regex("""\b(?:class|interface|object)\s+([A-Za-z_][A-Za-z0-9_]*)\b""")
        )
        return patterns.flatMap { pattern ->
            pattern.findAll(source).map { match ->
                Declaration(
                    name = match.groupValues[1],
                    line = lineNumber(source, match.range.first)
                )
            }
        }
    }

    private fun lineNumber(source: String, index: Int): Int = source
        .substring(0, index.coerceIn(0, source.length))
        .count { it == '\n' } + 1

    private data class Declaration(
        val name: String,
        val line: Int
    )
}
