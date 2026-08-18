// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LitShadowUniformLayout
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoAxis
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/** Rendered at 2x the 64dp size it's composited at ([StudioShell.kt]'s
 * `ORIENTATION_GIZMO_SIZE`) -- supersampled so the thin axis shafts don't look jagged at their
 * displayed size. Still tiny next to the main viewport's own render target. */
private const val GIZMO_SIZE = 128

/** How far the synthetic gizmo camera sits from the origin -- only its FOV/distance ratio
 * matters (how much of the frame the axes fill), not the absolute value. */
private const val GIZMO_CAMERA_DISTANCE = 4f
private const val GIZMO_FOV_RADIANS = 0.5f
private const val GIZMO_NEAR = 0.1f
private const val GIZMO_FAR = 10f

private const val AXIS_LENGTH = 0.9f
private const val SHAFT_HALF_THICKNESS = 0.03f
private const val TIP_HALF_SIZE = 0.14f

private val TRANSPARENT = floatArrayOf(0f, 0f, 0f, 0f)

/**
 * Studio's rotating axis-orientation widget (Unity/Godot's top-right corner gizmo) --
 * [StudioCameraPreview]'s exact shape (one [RenderTarget]/[Material] pair, reused every frame),
 * except the "camera" it renders is a synthetic, translation-free one built fresh each frame
 * from [mainCamera]'s facing direction: same direction, fixed distance, always centered on the
 * origin -- so the widget spins with the viewport but never pans with it.
 *
 * Visual only (see the plan's Non-goals) -- clicking an axis to snap the camera is a follow-up,
 * not built here.
 */
internal class StudioOrientationGizmo {
    private var renderTarget: RenderTarget? = null
    private var meshes: List<Mesh>? = null
    private var axisMaterial: Material? = null

    /** The texture to composite, or `null` until [render] has run once. */
    var material: Material? = null
        private set

    fun render(renderer: Renderer, mainCamera: CoreCamera) {
        val target = renderTarget
            ?: renderer.createRenderTarget(GIZMO_SIZE, GIZMO_SIZE).also { renderTarget = it }
        if (material == null) material = renderer.createMaterial(renderTarget = target)
        // Not the default 24-float buffer: this mesh's PositionNormalColor format resolves to
        // the same primary/lit pipeline RotatingCubeDemo's cube uses, which writes the FULL
        // lit-shadow uniform layout (not just mvp+light) whenever shadowsEnabled is on -- a
        // smaller buffer here is a real vkMapMemory-oversteps-allocation error, not just wasted
        // space. Matches how every other PositionNormalColor material in Studio sizes itself.
        val flatMaterial = axisMaterial
            ?: renderer.createMaterial(LitShadowUniformLayout).also { axisMaterial = it }
        val axisMeshes = meshes ?: buildAxisMeshes(renderer).also { meshes = it }

        val direction = (mainCamera.center - mainCamera.eye).normalized()
        val gizmoCamera = CoreCamera(
            eye = direction * GIZMO_CAMERA_DISTANCE,
            center = Vec3.ZERO,
            up = mainCamera.up,
            fovYRadians = GIZMO_FOV_RADIANS,
            near = GIZMO_NEAR,
            far = GIZMO_FAR,
        )

        // sceneViewport, like StudioCameraPreview, would otherwise confine this offscreen pass
        // to the main viewport panel's window-coordinate rect instead of the gizmo's own target.
        val sceneViewport = renderer.sceneViewport
        renderer.sceneViewport = null
        // clearColor is a fresh read on every pass (no per-RenderTarget override exists), so a
        // transparent widget means swapping it out around just this call -- the lit shader
        // writes alpha=1 for the axis geometry itself, so only the untouched background pixels
        // end up transparent, not the axes.
        val clearColor = renderer.clearColor
        renderer.clearColor = TRANSPARENT
        renderer.renderToTexture(target, gizmoCamera, axisMeshes.map { DrawCall(it, flatMaterial) })
        renderer.clearColor = clearColor
        renderer.sceneViewport = sceneViewport
    }

    /**
     * Destroys both materials and every axis [Mesh] -- unlike [renderTarget], none of these are
     * tracked/auto-destroyed by [Renderer.destroy] (see [StudioCameraPreview.dispose]'s own
     * comment for the same distinction: a `RenderTarget` is tracked, a `Material` is not; the
     * same is true of a [Mesh]). [renderTarget] itself is deliberately not destroyed here for
     * that reason -- only nulled, so a later [render] call rebuilds through the renderer's own
     * tracked path rather than double-destroying it.
     */
    fun dispose() {
        material?.destroy()
        material = null
        axisMaterial?.destroy()
        axisMaterial = null
        meshes?.forEach { it.destroy() }
        meshes = null
        renderTarget = null
    }
}

/** One shaft + one tip [Mesh] per [GizmoAxis], reusing [GizmoAxis]'s own red/green/blue so the
 * widget matches the transform gizmo's existing axis colors. */
private fun buildAxisMeshes(renderer: Renderer): List<Mesh> =
    GizmoAxis.entries.flatMap { axis ->
        val shaftHalfExtents = axis.direction.scaledHalfExtents(AXIS_LENGTH / 2f, SHAFT_HALF_THICKNESS)
        val tipHalfExtents = Vec3(TIP_HALF_SIZE, TIP_HALF_SIZE, TIP_HALF_SIZE)
        listOf(
            renderer.createMesh(boxGeometry(shaftHalfExtents, axis.direction * (AXIS_LENGTH / 2f), axis.color)),
            renderer.createMesh(boxGeometry(tipHalfExtents, axis.direction * AXIS_LENGTH, axis.color)),
        )
    }

/** The receiver's nonzero component picks [length] (the shaft's own axis) vs [thickness] (the
 * other two) -- e.g. `Vec3(1,0,0).scaledHalfExtents(l, t)` is `Vec3(l, t, t)`, a box long in X. */
private fun Vec3.scaledHalfExtents(length: Float, thickness: Float): Vec3 = Vec3(
    if (x != 0f) length else thickness,
    if (y != 0f) length else thickness,
    if (z != 0f) length else thickness,
)

/** An axis-aligned box baked directly into vertex data at [center] with [halfExtents] and a
 * flat [color] -- not a unit cube plus a [io.github.ronjunevaldoz.awake.core.math.Mat4] scale,
 * so [StudioOrientationGizmo]'s draw calls need only a `Mat4()` identity model. Reuses the exact
 * 24-vertex/6-face template `RotatingCubeMesh.kt`'s `studioCubeGeometry` uses (same winding/
 * normals, proven correct) rather than deriving a new one -- only the position scale/offset and
 * color per vertex differ. */
@Suppress("MagicNumber")
private fun boxGeometry(halfExtents: Vec3, center: Vec3, color: FloatArray): MeshGeometry {
    val vertices = FloatArray(UNIT_BOX_TEMPLATE.size)
    var i = 0
    while (i < UNIT_BOX_TEMPLATE.size) {
        vertices[i] = UNIT_BOX_TEMPLATE[i] * 2f * halfExtents.x + center.x
        vertices[i + 1] = UNIT_BOX_TEMPLATE[i + 1] * 2f * halfExtents.y + center.y
        vertices[i + 2] = UNIT_BOX_TEMPLATE[i + 2] * 2f * halfExtents.z + center.z
        vertices[i + 3] = UNIT_BOX_TEMPLATE[i + 3]
        vertices[i + 4] = UNIT_BOX_TEMPLATE[i + 4]
        vertices[i + 5] = UNIT_BOX_TEMPLATE[i + 5]
        vertices[i + 6] = color[0]
        vertices[i + 7] = color[1]
        vertices[i + 8] = color[2]
        i += 9
    }
    return MeshGeometry(vertices, UNIT_BOX_INDICES, format = VertexFormat.PositionNormalColor)
}

// Unit cube (-0.5..0.5), 4 vertices/face x 6 faces -- same template `RotatingCubeMesh.kt`'s
// cubeVertices/cubeIndices use (see that file's own comment on why 24, not 8, shared corners).
@Suppress("MagicNumber")
private val UNIT_BOX_TEMPLATE = floatArrayOf(
    -0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 0f, 0f, 0f,
    0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 0f, 0f, 0f,
    0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 0f, 0f, 0f,
    -0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 0f, 0f, 0f,
    -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, 0f,
    0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, 0f,
    0.5f, 0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, 0f,
    -0.5f, 0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, 0f,
    -0.5f, -0.5f, -0.5f, -1f, 0f, 0f, 0f, 0f, 0f,
    -0.5f, 0.5f, -0.5f, -1f, 0f, 0f, 0f, 0f, 0f,
    -0.5f, 0.5f, 0.5f, -1f, 0f, 0f, 0f, 0f, 0f,
    -0.5f, -0.5f, 0.5f, -1f, 0f, 0f, 0f, 0f, 0f,
    0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 0f, 0f, 0f,
    0.5f, -0.5f, 0.5f, 1f, 0f, 0f, 0f, 0f, 0f,
    0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 0f, 0f, 0f,
    0.5f, 0.5f, -0.5f, 1f, 0f, 0f, 0f, 0f, 0f,
    -0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 0f, 0f, 0f,
    -0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0f, 0f, 0f,
    0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0f, 0f, 0f,
    0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 0f, 0f, 0f,
    -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 0f, 0f,
    0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 0f, 0f,
    0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 0f, 0f, 0f,
    -0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 0f, 0f, 0f,
)

@Suppress("MagicNumber")
private val UNIT_BOX_INDICES = intArrayOf(
    0, 1, 2, 2, 3, 0,
    4, 5, 6, 6, 7, 4,
    8, 9, 10, 10, 11, 8,
    12, 13, 14, 14, 15, 12,
    16, 17, 18, 18, 19, 16,
    20, 21, 22, 22, 23, 20,
)
