// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.systems

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.inverse
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SHADOW_FAR
import io.github.ronjunevaldoz.awake.render.renderer.SHADOW_NEAR
import io.github.ronjunevaldoz.awake.render.renderer.SHADOW_ORTHO_HALF_SIZE
import io.github.ronjunevaldoz.awake.render.renderer.boundsDebugLines
import io.github.ronjunevaldoz.awake.render.renderer.directionalShadowBox
import io.github.ronjunevaldoz.awake.render.renderer.frustumDebugLines
import io.github.ronjunevaldoz.awake.render.renderer.lightGizmoLines
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshBounds
import io.github.ronjunevaldoz.awake.scene.rendering.components.Occluder
import io.github.ronjunevaldoz.awake.scene.rendering.components.WorldDebugSettings

/**
 * Draws the camera frustum and/or [MeshBounds] boxes as world-space wireframes, gated by
 * [WorldDebugSettings] -- kept separate from [RenderSystem] (which already does draw-call
 * assembly, culling, and LOD selection) so this stays a single-responsibility opt-in, same
 * "own component, own system" shape [InstancedMeshRenderer]/[LodGroup] already established
 * rather than growing [RenderSystem.update] further. Run this after [RenderSystem] each frame
 * -- both call `renderer.draw`/`drawDebugLines` independently, order between them doesn't
 * matter for correctness (only which one's lines end up in this frame's line buffer, and
 * [Renderer.drawDebugLines] replaces the whole buffer each call, not appends).
 */
class DebugVisualizationSystem(
    private val renderer: Renderer,
) : System {
    private val lines = ArrayList<LineSegment>()

    override fun update(world: World, delta: Float) {
        val settings = world.family<WorldDebugSettings>().components().firstOrNull() ?: return
        if (!settings.showFrustum && !settings.showBounds && !settings.showOcclusion &&
            !settings.showLights && !settings.showShadowFrustum
        ) {
            return
        }
        lines.clear()
        // Deliberately NOT primaryCamera -- see WorldDebugSettings.frustumTargetEntityId's own
        // doc comment for why drawing the viewport's own camera's frustum is invisible by
        // construction. No target (or a target with no Camera) draws nothing, rather than
        // falling back to primaryCamera and reintroducing that same invisible case.
        if (settings.showFrustum) {
            settings.frustumTargetEntityId
                ?.let { targetId -> world.cameraOf(targetId) }
                ?.let { lines += frustumDebugLines(it.camera, CONSERVATIVE_ASPECT, FRUSTUM_COLOR) }
        }
        if (settings.showBounds) {
            world.family<Transform, MeshBounds>().forEach { _, transform, bounds ->
                lines += boundsDebugLines(bounds.localBounds, transform.worldMatrix, BOUNDS_COLOR)
            }
        }
        // Visualizes which boxes are occluders, not per-entity occluded/visible state --
        // RenderSystem.lastOccludedCount already gives a numeric "did this cull anything"
        // signal, so this stays geometry-only, same scope showBounds already has.
        if (settings.showOcclusion) {
            world.family<Transform, Occluder>().forEach { _, transform, occluder ->
                lines += boundsDebugLines(occluder.localBounds, transform.worldMatrix, OCCLUDER_COLOR)
            }
        }
        if (settings.showLights || settings.showShadowFrustum) {
            // Falls back to DEFAULT_SCENE_LIGHT, same as RenderSystem.sceneLight() -- a scene
            // with no Light entity (most of Studio's examples) still shades with that default
            // direction, so the debugger should show the direction that's actually lighting it.
            val light = world.family<Light>().components().firstOrNull()
            val direction = light?.direction ?: DEFAULT_SCENE_LIGHT.direction
            val box = directionalShadowBox(direction, renderer.clipSpace)
            if (settings.showLights) {
                lines += lightGizmoLines(box.eye, direction, LIGHT_COLOR)
            }
            if (settings.showShadowFrustum) {
                val localBox = Aabb(
                    Vec3(-SHADOW_ORTHO_HALF_SIZE, -SHADOW_ORTHO_HALF_SIZE, -SHADOW_FAR),
                    Vec3(SHADOW_ORTHO_HALF_SIZE, SHADOW_ORTHO_HALF_SIZE, -SHADOW_NEAR),
                )
                box.view.inverse()?.let { lines += boundsDebugLines(localBox, it, SHADOW_COLOR) }
            }
        }
        renderer.drawDebugLines(lines)
    }
}

/** The [Camera] component on the entity with [entityId], or `null` when that entity has none --
 * same lookup `StudioShell.kt`'s own (Studio-private) `cameraOf` does, duplicated here since
 * this module is engine-neutral and can't depend on a Studio-only helper. */
private fun World.cameraOf(entityId: Int): Camera? {
    var found: Camera? = null
    family<Camera>().forEach { entity, camera -> if (entity.id == entityId) found = camera }
    return found
}

private val FRUSTUM_COLOR = floatArrayOf(1f, 1f, 0f, 1f)
private val BOUNDS_COLOR = floatArrayOf(0f, 1f, 0f, 1f)
private val OCCLUDER_COLOR = floatArrayOf(1f, 0.5f, 0f, 1f)
private val LIGHT_COLOR = floatArrayOf(1f, 0.75f, 0f, 1f)
private val SHADOW_COLOR = floatArrayOf(1f, 0f, 0.75f, 1f)
