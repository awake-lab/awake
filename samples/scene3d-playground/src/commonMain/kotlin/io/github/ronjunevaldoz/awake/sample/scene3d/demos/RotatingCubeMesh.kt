// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry

// Same interleaved position(vec3) + color(vec3) + uv(vec2) layout the (now-retired)
// hello-cube sample's own SampleMesh.kt used -- copied here rather than promoted to a shared
// module since [RotatingCubeDemo] is still this geometry's only consumer (see
// docs/reference/dsl-modules.md's scene-module ownership notes); promote if a second demo
// needs the same cube/grid geometry.
private val cubeVertices = floatArrayOf(
    -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
    0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
    0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
    -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
    -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
    0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
    0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
    -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
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
internal val rotatingCubeGeometry = MeshGeometry(cubeVertices, cubeIndices)

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
