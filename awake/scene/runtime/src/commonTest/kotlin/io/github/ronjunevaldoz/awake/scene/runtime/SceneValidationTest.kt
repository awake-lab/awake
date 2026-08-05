// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SceneValidationTest {

    @Test
    fun validatorReportsDuplicateNamesAndInvalidCamera() {
        val document = SceneDocument(
            name = "invalid",
            nodes = listOf(
                SceneNode(
                    name = "camera",
                    camera = SceneCamera(fovYDegrees = 200f, near = 0f, far = 0f)
                ),
                SceneNode(
                    name = "camera",
                    meshRenderer = SceneMeshRenderer(mesh = "", material = "")
                )
            )
        )

        val issues = SceneValidator.validate(document)

        assertTrue(issues.any { "duplicate node name" in it.message })
        assertTrue(issues.any { "camera.near" in it.message })
        assertTrue(issues.any { "camera.fovYDegrees" in it.message })
        assertTrue(issues.any { "meshRenderer.mesh" in it.message })
        assertTrue(issues.any { "meshRenderer.material" in it.message })
    }

    @Test
    fun requireValidThrowsOnInvalidScene() {
        val document = SceneDocument(
            nodes = listOf(
                SceneNode(
                    name = "bad-camera",
                    camera = SceneCamera(near = 10f, far = 1f)
                )
            )
        )

        val exception = assertFailsWith<SceneValidationException> {
            SceneValidator.requireValid(document)
        }
        assertEquals(1, exception.issues.size)
        assertTrue(exception.message.orEmpty().contains("bad-camera"))
    }
}
