// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): moved here (from `awake-vulkan`)
 * unchanged -- this is the actual backend-neutral data `RenderSystem` constructs.
 *
 * One draw: a [mesh] bound to a [material] (its descriptor set, and the uniform buffer
 * [Renderer.draw] writes this draw's MVP matrix into), placed in the world by [model].
 * Multiple `DrawCall`s can share the same [mesh] or [material] instance -- `Renderer` doesn't
 * assume either is unique per call.
 *
 * [extraUniformFloats] is appended after the MVP matrix (and, for [mesh]es using the
 * renderer's primary lit format, after the light floats) when writing [material]'s uniform
 * buffer -- empty by default (a plain-colored mesh needs nothing extra), a joint-palette
 * `FloatArray` for a GPU-skinned [mesh]. Which extra data (if any) a given [mesh.format]
 * expects is a backend concern, not something `DrawCall` itself interprets.
 */
data class DrawCall(
    val mesh: Mesh,
    val material: Material,
    val model: Mat4 = Mat4(),
    val extraUniformFloats: FloatArray = EMPTY_UNIFORM_FLOATS,
)

private val EMPTY_UNIFORM_FLOATS = FloatArray(0)
