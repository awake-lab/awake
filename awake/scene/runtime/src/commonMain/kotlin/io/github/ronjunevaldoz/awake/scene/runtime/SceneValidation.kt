// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

data class SceneValidationIssue(
    val path: String,
    val message: String,
)

class SceneValidationException(
    val issues: List<SceneValidationIssue>,
) : IllegalArgumentException(
    issues.joinToString(
        prefix = "Invalid scene document:\n",
        separator = "\n",
    ) { issue -> "- ${issue.path}: ${issue.message}" },
)

object SceneValidator {
    fun validate(document: SceneDocument): List<SceneValidationIssue> {
        val issues = ArrayList<SceneValidationIssue>()
        val namedPaths = LinkedHashMap<String, String>()
        document.nodes.forEachIndexed { index, node ->
            validateNode(node, path = nodePath(node.name, index, null), issues = issues, namedPaths = namedPaths)
        }
        return issues
    }

    fun requireValid(document: SceneDocument) {
        val issues = validate(document)
        if (issues.isNotEmpty()) {
            throw SceneValidationException(issues)
        }
    }

    private fun validateNode(
        node: SceneNode,
        path: String,
        issues: MutableList<SceneValidationIssue>,
        namedPaths: MutableMap<String, String>,
    ) {
        node.name?.takeIf { it.isNotBlank() }?.let { name ->
            val previous = namedPaths[name]
            if (previous == null) {
                namedPaths[name] = path
            } else {
                issues += SceneValidationIssue(path, "duplicate node name '$name' already used at $previous")
            }
        }

        node.meshRenderer?.let { meshRenderer ->
            if (meshRenderer.mesh.isBlank()) {
                issues += SceneValidationIssue(path, "meshRenderer.mesh must not be blank")
            }
            if (meshRenderer.material.isBlank()) {
                issues += SceneValidationIssue(path, "meshRenderer.material must not be blank")
            }
        }

        node.camera?.let { camera ->
            if (camera.near <= 0f) {
                issues += SceneValidationIssue(path, "camera.near must be > 0")
            }
            if (camera.far <= camera.near) {
                issues += SceneValidationIssue(path, "camera.far must be greater than camera.near")
            }
            if (camera.fovYDegrees <= 0f || camera.fovYDegrees >= 180f) {
                issues += SceneValidationIssue(path, "camera.fovYDegrees must be between 0 and 180")
            }
        }

        node.children.forEachIndexed { index, child ->
            validateNode(child, nodePath(child.name, index, path), issues, namedPaths)
        }
    }

    private fun nodePath(name: String?, index: Int, parent: String?): String {
        val segment = name?.takeIf { it.isNotBlank() } ?: "#$index"
        return if (parent == null) segment else "$parent/$segment"
    }
}
