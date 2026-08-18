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
 * are ignored in that case. A backend with no instanced pipeline for [mesh.format] skips the
 * draw call entirely, same "unknown format, skip" behavior an unmatched [mesh.format] already
 * has.
 *
 * [instanceJointPalettes] is the animated-instancing counterpart to [instanceModels] -- `null`
 * by default, and only meaningful alongside a non-null [instanceModels] of the same size (one
 * joint palette per instance, index-for-index). A [mesh.format] doesn't fit a whole joint
 * palette (up to a few KB) into the same per-instance vertex attribute [instanceModels] uses --
 * a backend with a skinned-instanced pipeline for [mesh.format] instead uploads these into a
 * storage buffer indexed by instance, alongside [instanceModels]'s own instance-rate vertex
 * buffer. `extraUniformFloats`'s single-instance joint palette and this list are mutually
 * exclusive in practice (a `DrawCall` is either one plain draw, one static-instanced batch, or
 * one animated-instanced batch), but nothing here enforces that -- it's a backend concern, same
 * as [extraUniformFloats]'s own doc comment already notes for format-specific interpretation.
 *
 * [instanceAlphas] is the billboard-particle counterpart to [instanceModels] -- `null` by
 * default, one alpha per instance, index-for-index, same "only meaningful alongside a
 * same-size [instanceModels]" contract as [instanceJointPalettes]. One `f32`/instance fits an
 * ordinary instance-rate vertex attribute (unlike a joint palette), so a backend with a
 * particle-instanced pipeline for [mesh.format] uploads this into its own small per-frame
 * vertex buffer rather than a storage buffer.
 */
data class DrawCall(
    val mesh: Mesh,
    val material: Material,
    val model: Mat4 = Mat4(),
    val extraUniformFloats: FloatArray = EMPTY_UNIFORM_FLOATS,
    val instanceModels: List<Mat4>? = null,
    val instanceJointPalettes: List<FloatArray>? = null,
    val instanceAlphas: List<Float>? = null,
)

private val EMPTY_UNIFORM_FLOATS = FloatArray(0)
