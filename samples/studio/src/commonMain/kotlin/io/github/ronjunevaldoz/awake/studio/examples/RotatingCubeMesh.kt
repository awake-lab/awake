// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber")

package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat

// Copied from samples/scene3d-playground's RotatingCubeMesh.kt -- 24 vertices (4 per face x 6
// faces), not 8 shared corners: a shared corner can only carry one normal, and approximating
// with corner-position-as-normal made flat faces look domed (a real regression there).
private val cubeVertices = floatArrayOf(
    -0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 0f, 0f, 0f,
    0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 1f, 0f, 0f,
    0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 1f, 1f, 0f,
    -0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 0f, 1f, 0f,
    -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, 1f,
    0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 1f, 0f, 1f,
    0.5f, 0.5f, 0.5f, 0f, 0f, 1f, 1f, 1f, 1f,
    -0.5f, 0.5f, 0.5f, 0f, 0f, 1f, 0f, 1f, 1f,
    -0.5f, -0.5f, -0.5f, -1f, 0f, 0f, 0f, 0f, 0f,
    -0.5f, 0.5f, -0.5f, -1f, 0f, 0f, 0f, 1f, 0f,
    -0.5f, 0.5f, 0.5f, -1f, 0f, 0f, 0f, 1f, 1f,
    -0.5f, -0.5f, 0.5f, -1f, 0f, 0f, 0f, 0f, 1f,
    0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, 0f,
    0.5f, -0.5f, 0.5f, 1f, 0f, 0f, 1f, 0f, 1f,
    0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 1f, 1f, 1f,
    0.5f, 0.5f, -0.5f, 1f, 0f, 0f, 1f, 1f, 0f,
    -0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 0f, 0f, 0f,
    -0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0f, 0f, 1f,
    0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 1f, 0f, 1f,
    0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 1f, 0f, 0f,
    -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, 0f,
    0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 1f, 1f, 0f,
    0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 1f, 1f, 1f,
    -0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 0f, 1f, 1f,
)
private val cubeIndices = intArrayOf(
    0, 1, 2, 2, 3, 0,
    4, 5, 6, 6, 7, 4,
    8, 9, 10, 10, 11, 8,
    12, 13, 14, 14, 15, 12,
    16, 17, 18, 18, 19, 16,
    20, 21, 22, 22, 23, 20,
)

/** Unit cube (-0.5..0.5 on every axis). */
internal val studioCubeGeometry = MeshGeometry(cubeVertices, cubeIndices, format = VertexFormat.PositionNormalColor)

private const val GROUND_PLANE_HALF_SIZE = 5f
private val groundPlaneVertices = floatArrayOf(
    -GROUND_PLANE_HALF_SIZE, 0f, -GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
    GROUND_PLANE_HALF_SIZE, 0f, -GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
    GROUND_PLANE_HALF_SIZE, 0f, GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
    -GROUND_PLANE_HALF_SIZE, 0f, GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
)
private val groundPlaneIndices = intArrayOf(0, 1, 2, 2, 3, 0)

internal val studioGroundGeometry =
    MeshGeometry(groundPlaneVertices, groundPlaneIndices, format = VertexFormat.PositionNormalColor)
