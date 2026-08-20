// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh.generate

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat

/**
 * Scoped builder for generating procedural [MeshGeometry] instances.
 */
class MeshGenerateScope {
    private var geometry: MeshGeometry? = null

    /**
     * Builds and validates the procedural [MeshGeometry].
     *
     * @return The constructed [MeshGeometry].
     */
    internal fun build(): MeshGeometry =
        requireNotNull(geometry) { "generate { } produced no geometry -- call cube()/plane() inside the block." }

    /**
     * Generates a unit or scaled cube mesh with 24 vertices and normals per face.
     *
     * @param size The edge length of the cube.
     * @param colored If true, generates distinct face colors; otherwise uniform white.
     * @param format The vertex layout format. Defaults to [VertexFormat.PositionNormalColor].
     */
    fun cube(
        size: Float = 1f,
        colored: Boolean = false,
        format: VertexFormat = VertexFormat.PositionNormalColor,
    ) {
        geometry = buildCubeGeometry(size = size, colored = colored, format = format)
    }

    /**
     * Generates a flat ground or quad plane mesh.
     *
     * @param size The full width and depth of the plane.
     * @param colored If true, uses a subtle ground tone; otherwise uniform white.
     * @param format The vertex layout format. Defaults to [VertexFormat.PositionNormalColor].
     */
    fun plane(
        size: Float = 10f,
        colored: Boolean = false,
        format: VertexFormat = VertexFormat.PositionNormalColor,
    ) {
        geometry = buildPlaneGeometry(size = size, colored = colored, format = format)
    }
}

/**
 * Entry point for constructing procedural [MeshGeometry].
 *
 * @param block Configuration lambda defining the procedural primitive.
 * @return The generated [MeshGeometry].
 */
fun generate(block: MeshGenerateScope.() -> Unit): MeshGeometry =
    MeshGenerateScope().apply(block).build()

private fun buildCubeGeometry(
    size: Float,
    colored: Boolean,
    format: VertexFormat,
): MeshGeometry {
    val h = size * 0.5f
    // 24 vertices (4 per face x 6 faces), 36 indices
    val vertices = if (colored) {
        floatArrayOf(
            // Back face (-Z)
            -h, -h, -h, 0f, 0f, -1f, 0f, 0f, 0f,
            h, -h, -h, 0f, 0f, -1f, 1f, 0f, 0f,
            h, h, -h, 0f, 0f, -1f, 1f, 1f, 0f,
            -h, h, -h, 0f, 0f, -1f, 0f, 1f, 0f,
            // Front face (+Z)
            -h, -h, h, 0f, 0f, 1f, 0f, 0f, 1f,
            h, -h, h, 0f, 0f, 1f, 1f, 0f, 1f,
            h, h, h, 0f, 0f, 1f, 1f, 1f, 1f,
            -h, h, h, 0f, 0f, 1f, 0f, 1f, 1f,
            // Left face (-X)
            -h, -h, -h, -1f, 0f, 0f, 0f, 0f, 0f,
            -h, h, -h, -1f, 0f, 0f, 0f, 1f, 0f,
            -h, h, h, -1f, 0f, 0f, 0f, 1f, 1f,
            -h, -h, h, -1f, 0f, 0f, 0f, 0f, 1f,
            // Right face (+X)
            h, -h, -h, 1f, 0f, 0f, 1f, 0f, 0f,
            h, -h, h, 1f, 0f, 0f, 1f, 0f, 1f,
            h, h, h, 1f, 0f, 0f, 1f, 1f, 1f,
            h, h, -h, 1f, 0f, 0f, 1f, 1f, 0f,
            // Bottom face (-Y)
            -h, -h, -h, 0f, -1f, 0f, 0f, 0f, 0f,
            -h, -h, h, 0f, -1f, 0f, 0f, 0f, 1f,
            h, -h, h, 0f, -1f, 0f, 1f, 0f, 1f,
            h, -h, -h, 0f, -1f, 0f, 1f, 0f, 0f,
            // Top face (+Y)
            -h, h, -h, 0f, 1f, 0f, 0f, 1f, 0f,
            h, h, -h, 0f, 1f, 0f, 1f, 1f, 0f,
            h, h, h, 0f, 1f, 0f, 1f, 1f, 1f,
            -h, h, h, 0f, 1f, 0f, 0f, 1f, 1f,
        )
    } else {
        floatArrayOf(
            // Back face (-Z)
            -h, -h, -h, 0f, 0f, -1f, 1f, 1f, 1f,
            h, -h, -h, 0f, 0f, -1f, 1f, 1f, 1f,
            h, h, -h, 0f, 0f, -1f, 1f, 1f, 1f,
            -h, h, -h, 0f, 0f, -1f, 1f, 1f, 1f,
            // Front face (+Z)
            -h, -h, h, 0f, 0f, 1f, 1f, 1f, 1f,
            h, -h, h, 0f, 0f, 1f, 1f, 1f, 1f,
            h, h, h, 0f, 0f, 1f, 1f, 1f, 1f,
            -h, h, h, 0f, 0f, 1f, 1f, 1f, 1f,
            // Left face (-X)
            -h, -h, -h, -1f, 0f, 0f, 1f, 1f, 1f,
            -h, h, -h, -1f, 0f, 0f, 1f, 1f, 1f,
            -h, h, h, -1f, 0f, 0f, 1f, 1f, 1f,
            -h, -h, h, -1f, 0f, 0f, 1f, 1f, 1f,
            // Right face (+X)
            h, -h, -h, 1f, 0f, 0f, 1f, 1f, 1f,
            h, -h, h, 1f, 0f, 0f, 1f, 1f, 1f,
            h, h, h, 1f, 0f, 0f, 1f, 1f, 1f,
            h, h, -h, 1f, 0f, 0f, 1f, 1f, 1f,
            // Bottom face (-Y)
            -h, -h, -h, 0f, -1f, 0f, 1f, 1f, 1f,
            -h, -h, h, 0f, -1f, 0f, 1f, 1f, 1f,
            h, -h, h, 0f, -1f, 0f, 1f, 1f, 1f,
            h, -h, -h, 0f, -1f, 0f, 1f, 1f, 1f,
            // Top face (+Y)
            -h, h, -h, 0f, 1f, 0f, 1f, 1f, 1f,
            h, h, -h, 0f, 1f, 0f, 1f, 1f, 1f,
            h, h, h, 0f, 1f, 0f, 1f, 1f, 1f,
            -h, h, h, 0f, 1f, 0f, 1f, 1f, 1f,
        )
    }
    val indices = intArrayOf(
        0, 1, 2, 2, 3, 0,
        4, 5, 6, 6, 7, 4,
        8, 9, 10, 10, 11, 8,
        12, 13, 14, 14, 15, 12,
        16, 17, 18, 18, 19, 16,
        20, 21, 22, 22, 23, 20,
    )
    return MeshGeometry(vertices = vertices, indices = indices, format = format)
}

private fun buildPlaneGeometry(
    size: Float,
    colored: Boolean,
    format: VertexFormat,
): MeshGeometry {
    val h = size * 0.5f
    val (r, g, b) = if (colored) Triple(0.5f, 0.5f, 0.55f) else Triple(1f, 1f, 1f)
    val vertices = floatArrayOf(
        -h, 0f, -h, 0f, 1f, 0f, r, g, b,
        h, 0f, -h, 0f, 1f, 0f, r, g, b,
        h, 0f, h, 0f, 1f, 0f, r, g, b,
        -h, 0f, h, 0f, 1f, 0f, r, g, b,
    )
    val indices = intArrayOf(0, 1, 2, 2, 3, 0)
    return MeshGeometry(vertices = vertices, indices = indices, format = format)
}
