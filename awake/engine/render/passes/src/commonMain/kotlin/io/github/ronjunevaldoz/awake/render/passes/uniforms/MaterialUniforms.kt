// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.uniforms

import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.UniformFields
import io.github.ronjunevaldoz.awake.render.renderer.UniformLayout

/**
 * Standard uniform layouts for lit and textured PBR rendering.
 */
object MaterialUniformLayouts {
    /** Basic untextured lit layout: MVP (16) + lightDir (4) + lightColor (4) + PBR factors (4) = 28 floats. */
    val Lit = UniformLayout(
        UniformFields.Mvp,
        UniformFields.LightDirection,
        UniformFields.LightColor,
        UniformFields.PbrFactors,
    )

    /** Full textured glTF PBR layout: MVP (16) + lightDir (4) + lightColor (4) + model (16) + cameraPos (4) + PBR factors (4) + baseColorFactor (4) + emissiveFactor (4) + fog (4) = 60 floats. */
    val PbrTextured = UniformLayout(
        UniformFields.Mvp,
        UniformFields.LightDirection,
        UniformFields.LightColor,
        UniformFields.Model,
        UniformFields.CameraPosition,
        UniformFields.PbrFactors,
        UniformFields.BaseColorFactor,
        UniformFields.EmissiveFactor,
        UniformFields.FogColor,
    )
}

val PBR_MATERIAL_FLOATS: Int = UniformFields.PbrFactors.floats
val PBR_TEXTURED_MATERIAL_FLOATS: Int =
    UniformFields.PbrFactors.floats + UniformFields.BaseColorFactor.floats + UniformFields.EmissiveFactor.floats

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
