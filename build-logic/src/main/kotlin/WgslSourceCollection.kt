import java.io.File
import org.gradle.api.GradleException

/** Gathers every `.wgsl` file under [primaryRoot] and each of [additionalRoots], pairing each
 * file with the root it came from (needed for `relativeTo(...)` -- see
 * [SyncWgslShaderPipelineTask]'s own doc comment on `additionalSourceDirectories`). A filename
 * that exists under more than one root throws immediately, naming both paths, rather than
 * silently letting one shadow the other -- the whole point of a shared root is a single
 * canonical file, so an ambiguous duplicate is a build error, not a "pick one" situation. */
internal fun collectWgslFiles(primaryRoot: File, additionalRoots: Set<File>): List<Pair<File, File>> {
    val roots = buildList {
        add(primaryRoot)
        addAll(additionalRoots)
    }
    val filesByName = mutableMapOf<String, File>()
    val result = mutableListOf<Pair<File, File>>()
    roots.forEach { root ->
        if (!root.exists()) return@forEach
        root.walkTopDown()
            .filter { it.isFile && it.extension == "wgsl" }
            .sortedBy { it.invariantSeparatorsPath }
            .forEach { file ->
                val existing = filesByName[file.name]
                if (existing != null) {
                    throw GradleException(
                        "Duplicate WGSL shader filename '${file.name}' found at both " +
                            "${existing.invariantSeparatorsPath} and ${file.invariantSeparatorsPath} -- " +
                            "remove the stray copy so there is exactly one canonical source."
                    )
                }
                filesByName[file.name] = file
                result += file to root
            }
    }
    return result.sortedBy { it.first.invariantSeparatorsPath }
}
