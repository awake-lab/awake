package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCameraComponent
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SceneLoaderTest {
    @Test
    fun documentRoundTripsThroughJson() {
        val document = SceneDocument(
            name = "round-trip",
            nodes = listOf(
                SceneNode(
                    name = "root",
                    transform = SceneTransform(
                        position = SceneVec3(1f, 2f, 3f),
                        rotation = SceneVec3(4f, 5f, 6f),
                        scale = SceneVec3(7f, 8f, 9f)
                    ),
                    children = listOf(
                        SceneNode(
                            name = "child",
                            meshRenderer = SceneMeshRenderer(mesh = "cube", material = "mat")
                        )
                    )
                )
            )
        )

        val encoded = SceneLoader.encode(document)
        val decoded = SceneLoader.decode(encoded)

        assertEquals(document, decoded)
    }

    @Test
    fun instantiateBuildsWorldHierarchyAndRenderableRequests() {
        val document = SceneDocument(
            name = "scene",
            nodes = listOf(
                SceneNode(
                    name = "camera",
                    camera = SceneCamera(fovYDegrees = 45f),
                    transform = SceneTransform(position = SceneVec3(0f, 0f, 5f))
                ),
                SceneNode(
                    name = "parent",
                    transform = SceneTransform(position = SceneVec3(1f, 0f, 0f)),
                    children = listOf(
                        SceneNode(
                            name = "child",
                            transform = SceneTransform(position = SceneVec3(0f, 2f, 0f)),
                            meshRenderer = SceneMeshRenderer(mesh = "cube", material = "mat")
                        )
                    )
                )
            )
        )

        val instance = document.instantiate(World())

        assertEquals(2, instance.roots.size)
        assertEquals(1, instance.renderableRequests.size)

        val cameraRoot = instance.roots[0]
        val parentRoot = instance.roots[1]
        val childNode = parentRoot.children.single()
        assertEquals("camera", cameraRoot.name)
        assertEquals("parent", parentRoot.name)
        assertEquals("child", childNode.name)

        val world = instance.world
        val cameraEntity = cameraRoot.entity
        val parentEntity = parentRoot.entity
        val childEntity = childNode.entity

        val cameraName = world.get<Name>(cameraEntity)
        val cameraTransform = world.get<Transform>(cameraEntity)
        val camera = world.get<SceneCameraComponent>(cameraEntity)
        val parentTransform = world.get<Transform>(parentEntity)
        val childTransform = world.get<Transform>(childEntity)

        assertNotNull(cameraName)
        assertEquals("camera", cameraName.value)
        assertNotNull(cameraTransform)
        assertEquals(5f, cameraTransform.position.z)
        assertNotNull(camera)
        assertTrue(camera.isPrimary)
        assertEquals(PI / 4.0, camera.camera.fovYRadians.toDouble(), 0.0001)
        assertNotNull(parentTransform)
        assertNotNull(childTransform)
        assertEquals(parentEntity, childTransform.parent)
        assertEquals(1f, parentTransform.position.x)
        assertEquals(2f, childTransform.position.y)

        val request = instance.renderableRequests.single()
        assertEquals(childEntity, request.entity)
        assertEquals("cube", request.meshRenderer.mesh)
        assertEquals("mat", request.meshRenderer.material)
    }

    @Test
    fun loadFromResourceReadsBundledSceneJson() {
        val document = SceneLoader.loadFromResource("scenes/mvp.scene.json")

        assertEquals("mvp-scene", document.name)
        assertEquals(2, document.nodes.size)
        assertEquals("camera", document.nodes[0].name)
        assertEquals("cube", document.nodes[1].name)
        assertNotNull(document.nodes[1].meshRenderer)
    }
}
