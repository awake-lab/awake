// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.systems.RenderSystem
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem

/**
 * Encapsulates exactly what `GenericGameApplication` (`awake-engine-game`) used to do for
 * scene handling -- [World]/[SceneInstance] loading, [TransformSystem]/[RenderSystem]
 * driving -- as a plain class a game constructs and drives itself (see docs/MVP_PLAN.md's
 * Decision Log, "GenericGameApplication a standalone render bootstrap"). Only takes a
 * [Renderer] (not pre-built mesh/material instances): a caller supplies its own
 * `resolveRenderable` closure that calls `renderer.createMesh(...)`/
 * `renderer.createMaterial(...)` for whatever the scene needs, at [load] time.
 */
@Deprecated(
    message = "Use SceneGameRuntime via awake:scene-dsl sceneGame/scene instead. " +
        "SceneRuntime is the legacy manual transform/render bootstrap."
)
class SceneRuntime(private val renderer: Renderer) {
    lateinit var world: World
        private set
    lateinit var scene: SceneInstance
        private set

    private val transformSystem = TransformSystem()
    private lateinit var renderSystem: RenderSystem

    /** Loads the scene document at [scenePath], instantiates it into a fresh [World], and
     * attaches renderable components via [resolveRenderable] -- called once, before [render]
     * is ever invoked. */
    suspend fun load(scenePath: String, resolveRenderable: (SceneRenderableRequest) -> MeshRenderer) {
        world = World()
        scene = SceneLoader.loadFromResource(scenePath).instantiate(world = world, flipYForClipSpace = renderer.flipYForClipSpace)
        scene.attachRenderableComponents(resolveRenderable)
        renderSystem = RenderSystem(renderer)
    }

    /** Propagates transforms then submits this frame's draw calls -- call once per frame,
     * after any game-specific per-frame logic (camera control, AI) has already mutated
     * [world]'s components. */
    fun render(delta: Float) {
        transformSystem.update(world, delta)
        renderSystem.update(world, delta)
    }
}
