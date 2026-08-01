// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

/** A vertex+fragment SPIR-V pair for one graphics pipeline -- replaces raw positional
 * `ByteArray` vert/frag constructor params across every pipeline in this module
 * ([RenderPipeline], [io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline], and
 * [io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer]'s 4 UI pipelines). All of those
 * raw params were the same type, so a caller could silently swap e.g. two pipelines' vertex/
 * fragment code (or a vertex and fragment array with each other) at a call site and still
 * compile, breaking rendering with no error; grouping each pipeline's vertex/fragment code
 * into its own [ShaderPair] makes that swap a compile error instead. */
data class ShaderPair(val vertex: ByteArray, val fragment: ByteArray)
