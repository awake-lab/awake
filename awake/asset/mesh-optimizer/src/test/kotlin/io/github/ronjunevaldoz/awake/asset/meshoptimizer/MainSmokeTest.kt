// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.meshoptimizer

import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** End-to-end: [GltfWriter] writes a synthetic cube, [main] decimates it, the output re-parses
 * via [GltfParser.parse] and has fewer triangles than the input -- proves the whole
 * read -> simplify -> write -> re-read pipeline round-trips, not just the algorithm in
 * isolation (already covered by `awake:core:geometry`'s own tests). */
class MainSmokeTest {
    private val cubePositions = floatArrayOf(
        -1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, -1f,
        -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1f,
    )
    private val cubeIndices = intArrayOf(
        0, 1, 2, 2, 3, 0,
        4, 6, 5, 6, 4, 7,
        0, 4, 5, 5, 1, 0,
        1, 5, 6, 6, 2, 1,
        2, 6, 7, 7, 3, 2,
        3, 7, 4, 4, 0, 3,
    )

    @Test
    fun decimateReducesTriangleCountAndOutputReparses() {
        val inputFile = File.createTempFile("mesh-optimizer-input", ".gltf")
        val outputFile = File.createTempFile("mesh-optimizer-output", ".gltf")
        try {
            inputFile.writeText(GltfWriter.writePositionOnlyMesh(cubePositions, cubeIndices))

            main(arrayOf(inputFile.absolutePath, outputFile.absolutePath, "0.5"))

            val reparsed = GltfParser.parse(outputFile.readText())
            val originalTriangleCount = cubeIndices.size / 3
            val decimatedTriangleCount = reparsed.indices.size / 3
            assertTrue(
                decimatedTriangleCount < originalTriangleCount,
                "expected fewer than $originalTriangleCount triangles, got $decimatedTriangleCount",
            )
        } finally {
            inputFile.delete()
            outputFile.delete()
        }
    }
}
