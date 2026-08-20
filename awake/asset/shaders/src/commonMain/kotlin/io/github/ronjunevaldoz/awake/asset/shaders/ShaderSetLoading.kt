// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.shaders

/** [stage]'s resource path, assuming (as every real [ShaderSource] this engine builds today
 * does) it's a [ShaderSource.ResourcePath] -- shared by both backends' bootstrap classes so
 * neither hand-duplicates this cast + error message. */
fun ShaderStages.resourcePath(stage: ShaderStage): String =
    (this[stage] as? ShaderSource.ResourcePath)?.path
        ?: error("Shader loading only supports ShaderSource.ResourcePath today (stage=$stage).")

/** [stage]'s entry point -- shared by both backends' bootstrap classes. */
fun ShaderStages.entryPoint(stage: ShaderStage): String =
    this[stage]?.entryPoint
        ?: error("No $stage stage registered in this ShaderStages.")

/** [name]'s pipeline build wrapped so a resource-not-found/shader-module-creation failure says
 * which pipeline actually failed instead of just a bare file path or native error code -- shared
 * by both backends' bootstrap classes (`VulkanEngine`/`WebGpuEngine`), each of which builds
 * several pipelines outside `buildRequestedPipelines`'s own shared per-request wrapping (shadow/
 * skybox/instanced/skinnedInstanced/particle/additional-format). */
suspend fun <T> withPipelineLoadContext(name: String, block: suspend () -> T): T =
    try {
        block()
    } catch (e: Exception) {
        throw IllegalStateException("Failed to build pipeline '$name': ${e.message}", e)
    }
