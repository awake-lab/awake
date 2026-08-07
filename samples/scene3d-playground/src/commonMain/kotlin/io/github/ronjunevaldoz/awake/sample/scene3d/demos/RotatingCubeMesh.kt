// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat

// Interleaved position(vec3) + normal(vec3) + color(vec3) layout -- matches
// VertexFormat.PositionNormalColor, which this sample's own triangle.wgsl shades with
// (Lambertian diffuse). 24 vertices (4 per face x 6 faces), NOT the 8 shared corners the old
// unlit version used -- each corner needs a different normal depending on which face it's
// part of, and a shared corner can only carry one normal. An earlier version tried
// approximating this with 8 shared corners using each corner's own normalized position as its
// normal (pointing diagonally, not perpendicular to any face) -- it compiled and ran, but every
// face's shading gradient curved smoothly across sharply different-facing corners, making a
// perfectly flat cube face look domed/warped instead of flat (a real reported regression, not
// a hypothetical one). Duplicating vertices per face and giving each face its own exact normal
// is the standard fix -- flat per-face shading, at the cost of 24 vertices instead of 8 (cheap
// for a demo cube).
private val cubeVertices = floatArrayOf(
    // back face (z = -0.5), normal (0, 0, -1)
    -0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 0f, 0f, 0f,
    0.5f, -0.5f, -0.5f, 0f, 0f, -1f, 1f, 0f, 0f,
    0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 1f, 1f, 0f,
    -0.5f, 0.5f, -0.5f, 0f, 0f, -1f, 0f, 1f, 0f,
    // front face (z = 0.5), normal (0, 0, 1)
    -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, 1f,
    0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 1f, 0f, 1f,
    0.5f, 0.5f, 0.5f, 0f, 0f, 1f, 1f, 1f, 1f,
    -0.5f, 0.5f, 0.5f, 0f, 0f, 1f, 0f, 1f, 1f,
    // left face (x = -0.5), normal (-1, 0, 0)
    -0.5f, -0.5f, -0.5f, -1f, 0f, 0f, 0f, 0f, 0f,
    -0.5f, 0.5f, -0.5f, -1f, 0f, 0f, 0f, 1f, 0f,
    -0.5f, 0.5f, 0.5f, -1f, 0f, 0f, 0f, 1f, 1f,
    -0.5f, -0.5f, 0.5f, -1f, 0f, 0f, 0f, 0f, 1f,
    // right face (x = 0.5), normal (1, 0, 0)
    0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, 0f,
    0.5f, -0.5f, 0.5f, 1f, 0f, 0f, 1f, 0f, 1f,
    0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 1f, 1f, 1f,
    0.5f, 0.5f, -0.5f, 1f, 0f, 0f, 1f, 1f, 0f,
    // bottom face (y = -0.5), normal (0, -1, 0)
    -0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 0f, 0f, 0f,
    -0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 0f, 0f, 1f,
    0.5f, -0.5f, 0.5f, 0f, -1f, 0f, 1f, 0f, 1f,
    0.5f, -0.5f, -0.5f, 0f, -1f, 0f, 1f, 0f, 0f,
    // top face (y = 0.5), normal (0, 1, 0)
    -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, 0f,
    0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 1f, 1f, 0f,
    0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 1f, 1f, 1f,
    -0.5f, 0.5f, 0.5f, 0f, 1f, 0f, 0f, 1f, 1f,
)
private val cubeIndices = intArrayOf(
    0, 1, 2, 2, 3, 0, // back
    4, 5, 6, 6, 7, 4, // front
    8, 9, 10, 10, 11, 8, // left
    12, 13, 14, 14, 15, 12, // right
    16, 17, 18, 18, 19, 16, // bottom
    20, 21, 22, 22, 23, 20, // top
)

/** Unit cube (-0.5..0.5 on every axis). */
internal val rotatingCubeGeometry = MeshGeometry(cubeVertices, cubeIndices, format = VertexFormat.PositionNormalColor)

// Flat, lit ground quad -- same GRID_SIZE the debug-line reference grid already draws
// (ReferenceGrid.kt), but an actual shaded mesh: debug lines aren't fragment-shaded, so they
// can't show a shadow landing on them. Gives the shadow pass a real surface to receive onto.
private const val GROUND_PLANE_HALF_SIZE = 5f
private val groundPlaneVertices = floatArrayOf(
    -GROUND_PLANE_HALF_SIZE, 0f, -GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
    GROUND_PLANE_HALF_SIZE, 0f, -GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
    GROUND_PLANE_HALF_SIZE, 0f, GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
    -GROUND_PLANE_HALF_SIZE, 0f, GROUND_PLANE_HALF_SIZE, 0f, 1f, 0f, 0.5f, 0.5f, 0.55f,
)
private val groundPlaneIndices = intArrayOf(0, 1, 2, 2, 3, 0)

internal val rotatingGroundPlaneGeometry =
    MeshGeometry(groundPlaneVertices, groundPlaneIndices, format = VertexFormat.PositionNormalColor)
