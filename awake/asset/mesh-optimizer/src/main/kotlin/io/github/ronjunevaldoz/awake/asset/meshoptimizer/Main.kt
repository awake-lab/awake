// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.meshoptimizer

import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.geometry.MeshSimplifier
import java.io.File

private const val DEFAULT_TARGET_RATIO = 0.5f

/**
 * CLI wrapper around [MeshSimplifier] -- I/O only, the algorithm itself lives in
 * `awake:core:geometry` with zero glTF dependency (see that module's own doc comments).
 * Usage: `<input.gltf> <output.gltf> [targetRatio, default $DEFAULT_TARGET_RATIO]`.
 */
fun main(args: Array<String>) {
    require(args.size >= 2) { "Usage: <input.gltf> <output.gltf> [targetRatio]" }
    val inputPath = args[0]
    val outputPath = args[1]
    val targetRatio = args.getOrNull(2)?.toFloat() ?: DEFAULT_TARGET_RATIO

    val inputJson = File(inputPath).readText()
    val mesh = GltfParser.parse(inputJson)
    val originalTriangleCount = mesh.indices.size / 3

    val result = MeshSimplifier.simplify(mesh.positions, mesh.indices, targetRatio)
    val newTriangleCount = result.indices.size / 3

    File(outputPath).writeText(GltfWriter.writePositionOnlyMesh(result.positions, result.indices))

    println(
        "Simplified $inputPath ($originalTriangleCount triangles) -> $outputPath " +
            "($newTriangleCount triangles, ${result.positions.size / 3} vertices).",
    )
}
