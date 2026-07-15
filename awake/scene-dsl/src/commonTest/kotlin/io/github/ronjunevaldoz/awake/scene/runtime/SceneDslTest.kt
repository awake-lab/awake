// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCameraComponent
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SceneDslTest {

    @Test
    fun sceneDslBuildsDocumentShape() {
        val document = scene(name = "demo-scene") {
            entity("camera") {
                transform {
                    position(0f, 2f, 6f)
                }
                camera {
                    eye(0f, 2f, 6f)
                    center(0f, 0f, 0f)
                    perspective(fovYDegrees = 50f, near = 0.5f, far = 250f)
                }
            }
            entity("sun") {
                light {
                    directional()
                    color(1f, 0.9f, 0.8f)
                    intensity(2f)
                }
            }
            entity("cube") {
                transform {
                    position(1f, 0f, 0f)
                    scale(2f, 2f, 2f)
                }
                meshRenderer(mesh = "cube", material = "default")
                entity("child") {
                    transform {
                        position(0f, 1f, 0f)
                    }
                }
            }
        }

        assertEquals("demo-scene", document.name)
        assertEquals(3, document.nodes.size)
        assertEquals("camera", document.nodes[0].name)
        assertEquals(50f, document.nodes[0].camera?.fovYDegrees)
        assertEquals(SceneLight.Type.Directional, document.nodes[1].light?.type)
        assertEquals("cube", document.nodes[2].meshRenderer?.mesh)
        assertEquals("child", document.nodes[2].children.single().name)
    }

    @Test
    fun sceneDslInstantiatesThroughExistingLoader() {
        val document = scene("instantiated-scene") {
            entity("root") {
                transform {
                    position(1f, 0f, 0f)
                }
                entity("camera") {
                    transform {
                        position(0f, 0f, 5f)
                    }
                    camera {
                        perspective(fovYDegrees = 45f)
                    }
                }
                entity("light") {
                    light {
                        directional()
                    }
                }
                entity("cube") {
                    transform {
                        position(0f, 2f, 0f)
                    }
                    meshRenderer(mesh = "cube", material = "mat")
                }
            }
        }

        val instance = document.instantiate(flipYForClipSpace = true, world = World())
        val root = instance.roots.single()
        val rootChildren = root.children.associateBy { it.name }

        val world = instance.world
        val rootTransform = world.get<Transform>(root.entity)
        val cameraEntity = rootChildren.getValue("camera").entity
        val lightEntity = rootChildren.getValue("light").entity
        val cubeEntity = rootChildren.getValue("cube").entity

        assertNotNull(rootTransform)
        assertEquals(1f, rootTransform.position.x)
        assertEquals(root.entity, world.get<Transform>(cameraEntity)?.parent)
        assertEquals(root.entity, world.get<Transform>(lightEntity)?.parent)
        assertEquals(root.entity, world.get<Transform>(cubeEntity)?.parent)
        assertTrue(world.get<SceneCameraComponent>(cameraEntity)?.isPrimary == true)
        assertEquals(Light.Type.Directional, world.get<Light>(lightEntity)?.type)
        assertEquals("root", world.get<Name>(root.entity)?.value)

        val request = instance.renderableRequests.single()
        assertEquals(cubeEntity, request.entity)
        assertEquals("cube", request.meshRenderer.mesh)
        assertEquals("mat", request.meshRenderer.material)
    }

    @Test
    fun sceneDslCanInstantiateThroughCustomAdapter() {
        val document = scene("custom-adapter") {
            entity("root") {
                transform {
                    position(1f, 2f, 3f)
                }
                entity("camera") {
                    camera {
                        perspective(fovYDegrees = 45f)
                    }
                }
                entity("cube") {
                    meshRenderer(mesh = "cube", material = "default")
                }
            }
        }

        val adapter = RecordingSceneAdapter()
        val instance = SceneLoader.instantiate(document, flipYForClipSpace = true, adapter = adapter)

        assertEquals(3, instance.createdNodes.size)
        assertEquals(listOf("root"), instance.roots.map { it.name })
        assertEquals("root", instance.transforms.single { it.name == "root" }.name)
        assertTrue(instance.cameras.any { it.name == "camera" && it.flipYForClipSpace })
        assertTrue(instance.meshes.any { it.name == "cube" && it.mesh == "cube" && it.material == "default" })
    }
}

private class RecordingSceneAdapter : SceneInstantiationAdapter<RecordingNode, RecordingInstance> {
    private val createdNodes = ArrayList<RecordingNode>()
    private val transforms = ArrayList<RecordedTransform>()
    private val cameras = ArrayList<RecordedCamera>()
    private val lights = ArrayList<String>()
    private val meshes = ArrayList<RecordedMesh>()

    override fun createNode(node: SceneNode, parent: RecordingNode?): RecordingNode =
        RecordingNode(name = node.name, parent = parent?.name).also(createdNodes::add)

    override fun attachName(node: RecordingNode, name: String) = Unit

    override fun attachTransform(node: RecordingNode, transform: SceneTransform, parent: RecordingNode?) {
        transforms += RecordedTransform(node.name, parent?.name, transform.position)
    }

    override fun attachCamera(node: RecordingNode, camera: SceneCamera, flipYForClipSpace: Boolean) {
        cameras += RecordedCamera(node.name, flipYForClipSpace)
    }

    override fun attachLight(node: RecordingNode, light: SceneLight) {
        lights += node.name ?: "<unnamed>"
    }

    override fun queueMeshRenderer(node: RecordingNode, meshRenderer: SceneMeshRenderer) {
        meshes += RecordedMesh(node.name, meshRenderer.mesh, meshRenderer.material)
    }

    override fun complete(roots: List<SceneNodeHandle<RecordingNode>>): RecordingInstance = RecordingInstance(
        roots = roots,
        createdNodes = createdNodes.toList(),
        transforms = transforms.toList(),
        cameras = cameras.toList(),
        lights = lights.toList(),
        meshes = meshes.toList()
    )
}

private data class RecordingNode(val name: String?, val parent: String?)

private data class RecordedTransform(val name: String?, val parent: String?, val position: SceneVec3)

private data class RecordedCamera(val name: String?, val flipYForClipSpace: Boolean)

private data class RecordedMesh(val name: String?, val mesh: String, val material: String)

private data class RecordingInstance(
    val roots: List<SceneNodeHandle<RecordingNode>>,
    val createdNodes: List<RecordingNode>,
    val transforms: List<RecordedTransform>,
    val cameras: List<RecordedCamera>,
    val lights: List<String>,
    val meshes: List<RecordedMesh>
)
