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
 *
 * [instanceModels] is `null` by default (every existing caller draws exactly one copy of
 * [mesh] at [model], unchanged). When non-null, a backend with an instanced pipeline for
 * [mesh.format] draws `instanceModels.size` copies of [mesh] in one GPU call -- one call per
 * instance's own transform in [instanceModels] instead of [model] -- via
 * [io.github.ronjunevaldoz.awake.render.mesh.Mesh.drawInstanced]. [model]/[extraUniformFloats]
 * are ignored in that case (a joint palette doesn't fit an instance buffer -- animated/skinned
 * meshes aren't instanceable this way). A backend with no instanced pipeline for [mesh.format]
 * skips the draw call entirely, same "unknown format, skip" behavior an unmatched [mesh.format]
 * already has.
 */
data class DrawCall(
    val mesh: Mesh,
    val material: Material,
    val model: Mat4 = Mat4(),
    val extraUniformFloats: FloatArray = EMPTY_UNIFORM_FLOATS,
    val instanceModels: List<Mat4>? = null,
)

private val EMPTY_UNIFORM_FLOATS = FloatArray(0)
