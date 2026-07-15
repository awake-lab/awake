// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.scene

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry

internal const val starterVertexStride: Int = 8 * Float.SIZE_BYTES

private val cubeVertices = floatArrayOf(
    -1f, -1f, 1f, 0f, 0f, 1f, 0f, 0f,
    1f, -1f, 1f, 0f, 0f, 1f, 1f, 0f,
    1f, 1f, 1f, 0f, 0f, 1f, 1f, 1f,
    -1f, 1f, 1f, 0f, 0f, 1f, 0f, 1f,
    -1f, -1f, -1f, 0f, 0f, -1f, 0f, 0f,
    1f, -1f, -1f, 0f, 0f, -1f, 1f, 0f,
    1f, 1f, -1f, 0f, 0f, -1f, 1f, 1f,
    -1f, 1f, -1f, 0f, 0f, -1f, 0f, 1f
)

private val cubeIndices = intArrayOf(
    0, 1, 2, 2, 3, 0,
    1, 5, 6, 6, 2, 1,
    5, 4, 7, 7, 6, 5,
    4, 0, 3, 3, 7, 4,
    3, 2, 6, 6, 7, 3,
    4, 5, 1, 1, 0, 4
)

internal val starterCubeGeometry = MeshGeometry(cubeVertices, cubeIndices)
