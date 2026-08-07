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

        // A node could now legally carry two cameras -- the sealed-component shape trades that
        // type-level guarantee for not having to edit SceneNode per component, so it's checked
        // here instead.
        node.components.filterIsInstance<SceneCamera>().size.takeIf { it > 1 }?.let { count ->
            issues += SceneValidationIssue(path, "node declares $count cameras, expected at most 1")
        }

        node.components.forEach { component -> validateComponent(component, path, issues) }

        node.children.forEachIndexed { index, child ->
            validateNode(child, nodePath(child.name, index, path), issues, namedPaths)
        }
    }

    private fun validateComponent(
        component: SceneComponent,
        path: String,
        issues: MutableList<SceneValidationIssue>,
    ) {
        when (component) {
            is SceneMeshRenderer -> {
                if (component.mesh.isBlank()) {
                    issues += SceneValidationIssue(path, "meshRenderer.mesh must not be blank")
                }
                if (component.material.isBlank()) {
                    issues += SceneValidationIssue(path, "meshRenderer.material must not be blank")
                }
            }

            is SceneCamera -> {
                if (component.near <= 0f) {
                    issues += SceneValidationIssue(path, "camera.near must be > 0")
                }
                if (component.far <= component.near) {
                    issues += SceneValidationIssue(path, "camera.far must be greater than camera.near")
                }
                if (component.fovYDegrees <= 0f || component.fovYDegrees >= 180f) {
                    issues += SceneValidationIssue(path, "camera.fovYDegrees must be between 0 and 180")
                }
            }

            is ScenePbrMaterial -> {
                if (component.metallic !in 0f..1f) {
                    issues += SceneValidationIssue(path, "pbrMaterial.metallic must be within 0..1")
                }
                if (component.roughness !in 0f..1f) {
                    issues += SceneValidationIssue(path, "pbrMaterial.roughness must be within 0..1")
                }
            }

            is SceneLight -> Unit
        }
    }

    private fun nodePath(name: String?, index: Int, parent: String?): String {
        val segment = name?.takeIf { it.isNotBlank() } ?: "#$index"
        return if (parent == null) segment else "$parent/$segment"
    }
}
