// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package demo

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.DebugHud
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.navigation.createDemoNavMesh
import io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance
import io.github.ronjunevaldoz.awake.scene.systems.CameraFollowSystem
import io.github.ronjunevaldoz.awake.scene.systems.ChaseAiSystem
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.PlayerMovementSystem

/** The catalog debug tool's camera modes (see docs/MVP_PLAN.md's model-viewer/camera-
 * catalog decision log) -- [FOLLOW] is the pre-existing fixed third-person offset and stays
 * the default so nobody who doesn't touch the new UI dropdown sees a behavior change. */
enum class CameraMode { FOLLOW, ORBIT, FREE_FLY }

/**
 * Reusable-Application gap fix (see docs/MVP_PLAN.md's Decision Log): generic scene
 * loading/`TransformSystem`/`RenderSystem` wiring now lives in
 * `VulkanGameApplication`/`WebGpuGameApplication` (`awake-backend-vulkan`/
 * `awake-backend-webgpu`) -- this class only owns what's genuinely specific to *this* demo
 * game: resolving the player/camera/NPC entities by name and driving their
 * movement/camera-follow/chase-AI systems each fixed step. Both platform `Application`
 * subclasses construct this from their overridden `onSceneReady()` (once the base class's
 * `scene`/`world` are populated) and drive it from their overridden `onFixedUpdate`.
 *
 * Not `suspend`/no factory-vs-constructor split needed anymore -- all the `suspend` scene
 * loading already happened in the base class before `onSceneReady()` runs.
 */
internal class SceneRuntimeHost private constructor(
    private val world: World,
    private val cubeTransform: Transform,
    private val playerTransform: Transform,
    private val cameraComponent: Camera,
    npcTransform: Transform,
    groundTransform: Transform
) {
    private val playerMovementSystem = PlayerMovementSystem(playerTransform)
    private val cameraFollowSystem = CameraFollowSystem(playerTransform, cameraComponent)
    private val orbitCameraSystem = OrbitCameraSystem(playerTransform, cameraComponent)
    private val freeFlyCameraSystem = FreeFlyCameraSystem(cameraComponent)

    /** Which camera mode [fixedUpdate] drives this frame -- driven by the catalog tool's UI
     * dropdown (see `VulkanApplication`/`WebGpuApplication`'s `onDrawUi`). */
    var cameraMode: CameraMode = CameraMode.FOLLOW

    /** The meshes/entities the catalog tool's "focus" dropdown can pick between -- already-
     * loaded scene content, not arbitrary external models (see docs/MVP_PLAN.md's decision
     * log for why that's this slice's deliberate scope). */
    val catalogTargets: Map<String, Transform> = linkedMapOf(
        "player" to playerTransform,
        "cube" to cubeTransform,
        "ground" to groundTransform,
        "npc" to npcTransform
    )

    /** Retargets [orbitCameraSystem] to orbit around the named entry in [catalogTargets]. */
    var catalogTargetName: String = "player"
        set(value) {
            field = value
            orbitCameraSystem.target = catalogTargets.getValue(value)
        }
    // Null on platforms with no navmesh backend yet (iOS, wasmJs -- recast4j is plain JVM
    // only, see docs/MMORPG_ROADMAP.md's NavMesh decision). The NPC simply doesn't chase on
    // those platforms for now, matching how Material/Texture are TODO()-only on WebGPU.
    private val chaseAiSystem: ChaseAiSystem? = createDemoNavMesh()?.let { navMesh ->
        ChaseAiSystem(npcTransform, playerTransform, navMesh)
    }
    private var elapsedSeconds = 0f
    private var paused = false
    private var spaceWasDown = false

    /** Runs at a fixed [delta] (see [io.github.ronjunevaldoz.awake.core.application
     * .FixedTimestepLoop]) -- game-specific state only; the caller's own `onFixedUpdate`
     * runs [io.github.ronjunevaldoz.awake.scene.systems.TransformSystem] afterward. */
    fun fixedUpdate(delta: Float) {
        // Space toggles pause, on the rising edge only (not "while held") -- otherwise a
        // single held keypress would flip `paused` on/off dozens of times per second at a
        // 60Hz fixed step. A tiny, real, hardware-verifiable demonstration that Input
        // actually reaches the simulation, not just an isolated unit test.
        val spaceIsDown = Input.isKeyDown(Key.Space)
        if (spaceIsDown && !spaceWasDown) {
            paused = !paused
        }
        spaceWasDown = spaceIsDown

        if (paused) {
            return
        }
        elapsedSeconds += delta
        cubeTransform.rotation.y = elapsedSeconds
        cubeTransform.rotation.x = elapsedSeconds * 0.5f
        playerMovementSystem.update(world, delta)
        chaseAiSystem?.update(world, delta)
        when (cameraMode) {
            CameraMode.FOLLOW -> cameraFollowSystem.update(world, delta)
            CameraMode.ORBIT -> orbitCameraSystem.update(world, delta)
            CameraMode.FREE_FLY -> freeFlyCameraSystem.update(world, delta)
        }
        val position = playerTransform.position
        DebugHud.PlayerPositionText =
            "Pos: ${roundTo1dp(position.x)}, ${roundTo1dp(position.y)}, ${roundTo1dp(position.z)}"
    }

    private fun roundTo1dp(value: Float): Float {
        return kotlin.math.round(value * 10f) / 10f
    }

    /** A synthetic camera representing where [cameraFollowSystem] would put the eye/center
     * right now, independent of which [cameraMode] actually drives the shared
     * [cameraComponent] -- the catalog tool's frustum-wireframe toggle uses this to show
     * "what the follow camera would see" while looking at the scene from Orbit/Free-fly.
     * Not the live [cameraComponent] itself (that IS the active render camera, whichever
     * mode drives it) -- computed fresh from [playerTransform] and [FOLLOW_OFFSET], the
     * same offset [cameraFollowSystem] itself defaults to. */
    fun followCameraSnapshot(): CoreCamera {
        val live = cameraComponent.camera
        val position = playerTransform.position
        return CoreCamera(
            eye = Vec3(position.x + FOLLOW_OFFSET.x, position.y + FOLLOW_OFFSET.y, position.z + FOLLOW_OFFSET.z),
            center = Vec3(position.x, position.y, position.z),
            fovYRadians = live.fovYRadians,
            near = live.near,
            far = live.far
        )
    }

    companion object {
        fun create(scene: SceneInstance, world: World): SceneRuntimeHost {
            val cubeEntity = scene.rootEntity("cube")
                ?: error("MVP scene is missing a root node named 'cube'.")
            val playerEntity = scene.rootEntity("player")
                ?: error("MVP scene is missing a root node named 'player'.")
            val cameraEntity = scene.rootEntity("camera")
                ?: error("MVP scene is missing a root node named 'camera'.")
            val npcEntity = scene.rootEntity("npc")
                ?: error("MVP scene is missing a root node named 'npc'.")
            val groundEntity = scene.rootEntity("ground")
                ?: error("MVP scene is missing a root node named 'ground'.")
            val cubeTransform: Transform = world.get(cubeEntity)
                ?: error("'cube' node has no Transform.")
            val playerTransform: Transform = world.get(playerEntity)
                ?: error("'player' node has no Transform.")
            val cameraComponent: Camera = world.get(cameraEntity)
                ?: error("'camera' node has no Camera component.")
            val npcTransform: Transform = world.get(npcEntity)
                ?: error("'npc' node has no Transform.")
            val groundTransform: Transform = world.get(groundEntity)
                ?: error("'ground' node has no Transform.")
            return SceneRuntimeHost(
                world,
                cubeTransform,
                playerTransform,
                cameraComponent,
                npcTransform,
                groundTransform
            )
        }

        private fun SceneInstance.rootEntity(name: String): Entity? {
            return roots.firstOrNull { it.name == name }?.entity
        }

        /** Matches [CameraFollowSystem]'s own default offset -- kept in sync manually since
         * [followCameraSnapshot] computes the same formula independently (it must not run
         * the real system, which would mutate the shared [Camera] component). */
        private val FOLLOW_OFFSET = Vec3(0f, 3f, 6f)
    }
}
