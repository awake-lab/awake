// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import io.github.ronjunevaldoz.awake.scene.rendering.components.PbrMaterial
import kotlinx.coroutines.test.runTest
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera as SceneCameraComponent

class SceneLoaderTest {
    @Test
    fun fromWorldExportsLocalTransformsComponentsAndHierarchy() {
        val world = World()
        val parent = world.create()
        val child = world.create()
        world.add(parent, Name("parent"))
        world.add(parent, Transform(position = io.github.ronjunevaldoz.awake.core.math.Vec3(1f, 2f, 3f)))
        world.add(parent, PbrMaterial(metallic = 0.25f, roughness = 0.4f))
        world.add(
            parent,
            SpinControl().also {
                it.radians = 1.5f
                it.speed = 2f
            },
        )
        world.add(child, Name("child"))
        world.add(
            child,
            Transform(
                position = io.github.ronjunevaldoz.awake.core.math.Vec3(4f, 5f, 6f),
                parent = parent,
            ),
        )
        world.add(
            child,
            SceneCameraComponent(
                CoreCamera(
                    eye = io.github.ronjunevaldoz.awake.core.math.Vec3(7f, 8f, 9f),
                    center = io.github.ronjunevaldoz.awake.core.math.Vec3(1f, 2f, 3f),
                    fovYRadians = (PI / 3.0).toFloat(),
                    near = 0.1f,
                    far = 100f,
                ),
                isPrimary = false,
            ),
        )
        world.add(
            child,
            Light(
                color = io.github.ronjunevaldoz.awake.core.math.Vec3(0.1f, 0.2f, 0.3f),
                intensity = 0.75f,
                type = Light.Type.Point,
            ),
        )

        val exported = SceneLoader.fromWorld(world, name = "authored")

        assertEquals("authored", exported.name)
        assertEquals(
            listOf(
                SceneNode(
                    name = "parent",
                    transform = SceneTransform(position = SceneVec3(1f, 2f, 3f)),
                    components = listOf(
                        ScenePbrMaterial(metallic = 0.25f, roughness = 0.4f),
                        SceneSpinControl(radians = 1.5f, speed = 2f),
                    ),
                    children = listOf(
                        SceneNode(
                            name = "child",
                            transform = SceneTransform(position = SceneVec3(4f, 5f, 6f)),
                            components = listOf(
                                SceneCamera(
                                    eye = SceneVec3(7f, 8f, 9f),
                                    center = SceneVec3(1f, 2f, 3f),
                                    fovYDegrees = 60f,
                                    primary = false,
                                ),
                                SceneLight(
                                    color = SceneVec3(0.1f, 0.2f, 0.3f),
                                    intensity = 0.75f,
                                    type = SceneLight.Type.Point,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            exported.nodes,
        )
    }

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
                        scale = SceneVec3(7f, 8f, 9f),
                    ),
                    children = listOf(
                        SceneNode(
                            name = "child",
                            components = listOf(SceneMeshRenderer(mesh = "cube", material = "mat")),
                        ),
                    ),
                ),
            ),
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
                    components = listOf(SceneCamera(fovYDegrees = 45f)),
                    transform = SceneTransform(position = SceneVec3(0f, 0f, 5f)),
                ),
                SceneNode(
                    name = "parent",
                    transform = SceneTransform(position = SceneVec3(1f, 0f, 0f)),
                    children = listOf(
                        SceneNode(
                            name = "child",
                            transform = SceneTransform(position = SceneVec3(0f, 2f, 0f)),
                            components = listOf(
                                SceneMeshRenderer(mesh = "cube", material = "mat"),
                                ScenePbrMaterial(metallic = 0.25f, roughness = 0.4f),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val instance = document.instantiate(world = World())

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

        // The PbrMaterial on the same node must land as a real ECS component, not be dropped:
        // that silent drop is exactly what the old one-field-per-component shape allowed.
        val childPbr = world.get<PbrMaterial>(childEntity)
        assertNotNull(childPbr)
        assertEquals(0.25f, childPbr.metallic)
        assertEquals(0.4f, childPbr.roughness)

        val request = instance.renderableRequests.single()
        assertEquals(childEntity, request.entity)
        assertEquals("cube", request.meshRenderer.mesh)
        assertEquals("mat", request.meshRenderer.material)
    }

    @Test
    fun everyComponentTypeSurvivesARoundTrip() {
        // SceneLight in particular: its own `type` field collides with kotlinx's default
        // polymorphic discriminator, which is why SceneComponent declares a custom one.
        val document = SceneDocument(
            nodes = listOf(
                SceneNode(
                    name = "all",
                    components = listOf(
                        SceneCamera(fovYDegrees = 50f),
                        SceneLight(type = SceneLight.Type.Directional, intensity = 0.75f),
                        SceneMeshRenderer(mesh = "m", material = "mat"),
                        ScenePbrMaterial(metallic = 1f, roughness = 0.2f),
                        SceneSpinControl(radians = 1.5f, speed = 2f),
                    ),
                ),
            ),
        )

        assertEquals(document, SceneLoader.decode(SceneLoader.encode(document)))
    }

    @Test
    fun decodeRejectsADocumentFromANewerSchema() {
        val future = SceneLoader.encode(SceneDocument(version = SCENE_SCHEMA_VERSION + 1))

        val failure = assertFailsWith<SceneSchemaVersionException> { SceneLoader.decode(future) }

        assertEquals(SCENE_SCHEMA_VERSION + 1, failure.documentVersion)
    }

    @Test
    fun decodeAcceptsADocumentWithNoVersionField() {
        // Pre-versioning scenes have no `version` key; they must still load as version 1
        // rather than failing, since the shape they describe is unchanged.
        val decoded = SceneLoader.decode("""{ "name": "legacy", "nodes": [] }""")

        assertEquals(SCENE_SCHEMA_VERSION, decoded.version)
        assertEquals("legacy", decoded.name)
    }

    @Test
    fun loadFromResourceReadsBundledSceneJson() = runTest {
        val document = SceneLoader.loadFromResource("scenes/mvp.scene.json")

        assertEquals("mvp-scene", document.name)
        assertEquals(2, document.nodes.size)
        assertEquals("camera", document.nodes[0].name)
        assertEquals("cube", document.nodes[1].name)
        assertNotNull(
            document.nodes[1].components.filterIsInstance<SceneMeshRenderer>().singleOrNull(),
        )
        assertEquals(
            0.35f,
            document.nodes[1].components.filterIsInstance<ScenePbrMaterial>().single().roughness,
        )
    }
}
