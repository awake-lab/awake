// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.boundsDebugLines
import io.github.ronjunevaldoz.awake.render.renderer.frustumDebugLines
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
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
        if (!settings.showFrustum && !settings.showBounds && !settings.showOcclusion) return
        val camera = primaryCamera(world) ?: return

        lines.clear()
        if (settings.showFrustum) {
            lines += frustumDebugLines(camera.camera, CONSERVATIVE_ASPECT, FRUSTUM_COLOR)
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
        renderer.drawDebugLines(lines)
    }
}

private val FRUSTUM_COLOR = floatArrayOf(1f, 1f, 0f, 1f)
private val BOUNDS_COLOR = floatArrayOf(0f, 1f, 0f, 1f)
private val OCCLUDER_COLOR = floatArrayOf(1f, 0.5f, 0f, 1f)
