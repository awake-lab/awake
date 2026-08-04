// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat

// Interleaved position(vec3) + normal(vec3) + color(vec3) layout -- matches
// VertexFormat.PositionNormalColor, which this sample's own triangle.wgsl shades with
// (Lambertian diffuse, see that shader's own doc comment). Each corner's normal is just its
// own normalized position -- a centered unit cube's corner vector already points radially
// outward, so this is a cheap "rounded corner" shading approximation shared across the 3 faces
// that meet there, not true per-face flat shading (which would need 24 verts, one trio per
// face, instead of these 8 shared corners). Good enough for a demo cube's curvature cue; swap
// to duplicated per-face verts if flat-shaded faces are ever wanted instead.
private val cubeVertices = floatArrayOf(
    -0.5f, -0.5f, -0.5f, -0.577f, -0.577f, -0.577f, 0f, 0f, 0f, // v0
    0.5f, -0.5f, -0.5f, 0.577f, -0.577f, -0.577f, 1f, 0f, 0f, // v1
    0.5f, 0.5f, -0.5f, 0.577f, 0.577f, -0.577f, 1f, 1f, 0f, // v2
    -0.5f, 0.5f, -0.5f, -0.577f, 0.577f, -0.577f, 0f, 1f, 0f, // v3
    -0.5f, -0.5f, 0.5f, -0.577f, -0.577f, 0.577f, 0f, 0f, 1f, // v4
    0.5f, -0.5f, 0.5f, 0.577f, -0.577f, 0.577f, 1f, 0f, 1f, // v5
    0.5f, 0.5f, 0.5f, 0.577f, 0.577f, 0.577f, 1f, 1f, 1f, // v6
    -0.5f, 0.5f, 0.5f, -0.577f, 0.577f, 0.577f, 0f, 1f, 1f, // v7
)
private val cubeIndices = intArrayOf(
    0, 1, 2, 2, 3, 0, // back
    4, 5, 6, 6, 7, 4, // front
    0, 3, 7, 7, 4, 0, // left
    1, 5, 6, 6, 2, 1, // right
    0, 4, 5, 5, 1, 0, // bottom
    3, 2, 6, 6, 7, 3, // top
)

/** Unit cube (-0.5..0.5 on every axis). */
internal val rotatingCubeGeometry = MeshGeometry(cubeVertices, cubeIndices, format = VertexFormat.PositionNormalColor)

/** Local-space corners of [rotatingCubeGeometry], in the same winding order the mesh itself
 * uses -- reused by [RotatingCubeDemo] to build wireframe [io.github.ronjunevaldoz.awake.render.renderer.LineSegment]
 * edges when its "Wireframe" switch is on (see that switch's own wiring). */
internal val rotatingCubeLocalCorners = listOf(
    Triple(-0.5f, -0.5f, -0.5f), Triple(0.5f, -0.5f, -0.5f),
    Triple(0.5f, 0.5f, -0.5f), Triple(-0.5f, 0.5f, -0.5f),
    Triple(-0.5f, -0.5f, 0.5f), Triple(0.5f, -0.5f, 0.5f),
    Triple(0.5f, 0.5f, 0.5f), Triple(-0.5f, 0.5f, 0.5f),
)

/** 12 cube edges as corner-index pairs into [rotatingCubeLocalCorners]. */
internal val rotatingCubeEdgeIndices = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 0,
    4 to 5, 5 to 6, 6 to 7, 7 to 4,
    0 to 4, 1 to 5, 2 to 6, 3 to 7,
)
