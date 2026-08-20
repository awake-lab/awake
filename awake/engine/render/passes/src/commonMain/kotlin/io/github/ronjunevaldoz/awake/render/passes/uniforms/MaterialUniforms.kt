// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.uniforms

import io.github.ronjunevaldoz.awake.render.renderer.DrawCall

const val PBR_MATERIAL_FLOATS = 4
const val PBR_TEXTURED_MATERIAL_FLOATS = 12
const val DEFAULT_METALLIC = 0f
const val DEFAULT_ROUGHNESS = 0.5f
const val DEFAULT_METALLIC_FACTOR = 1f
const val DEFAULT_ROUGHNESS_FACTOR = 1f

/**
 * Packs `[metallic, roughness, 0, 0]` from [DrawCall.extraUniformFloats] for lit pipelines.
 */
fun pbrMaterialFloats(drawCall: DrawCall): FloatArray {
    val supplied = drawCall.extraUniformFloats
    if (supplied.size >= PBR_MATERIAL_FLOATS) return supplied.copyOf(PBR_MATERIAL_FLOATS)
    return floatArrayOf(DEFAULT_METALLIC, DEFAULT_ROUGHNESS, 0f, 0f)
}

/**
 * Packs `[metallic, roughness, pad, pad, baseColorFactor.rgba, emissiveFactor.rgb, pad]` (12 floats)
 * for textured PBR glTF pipelines.
 */
fun pbrTexturedMaterialFloats(drawCall: DrawCall): FloatArray {
    val supplied = drawCall.extraUniformFloats
    if (supplied.size >= PBR_TEXTURED_MATERIAL_FLOATS) return supplied.copyOf(PBR_TEXTURED_MATERIAL_FLOATS)
    return floatArrayOf(
        DEFAULT_METALLIC_FACTOR, DEFAULT_ROUGHNESS_FACTOR, 0f, 0f,
        1f, 1f, 1f, 1f,
        0f, 0f, 0f, 0f,
    )
}

/**
 * Packs `[fogColor.r, fogColor.g, fogColor.b, fogDensity]` into 4 floats.
 */
fun fogUniformFloats(fogColor: FloatArray, fogDensity: Float): FloatArray =
    floatArrayOf(
        if (fogColor.isNotEmpty()) fogColor[0] else 0f,
        if (fogColor.size > 1) fogColor[1] else 0f,
        if (fogColor.size > 2) fogColor[2] else 0f,
        fogDensity,
    )
