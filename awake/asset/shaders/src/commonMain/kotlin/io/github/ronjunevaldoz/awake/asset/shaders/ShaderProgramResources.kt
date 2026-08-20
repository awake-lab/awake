// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.shaders

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes

enum class ShaderStage {
    VERTEX,
    FRAGMENT,
    COMPUTE,
}

/** Where one shader stage's bytes actually come from -- [ResourcePath] (read from disk/assets
 * at load time) covers every real case in this engine today; [PrecompiledBinary]/[InlineText]
 * exist for a caller that already has bytes/source text in hand (e.g. a build step that embeds
 * a shader as a resource). [resolveBytes] turns any variant into the raw bytes a backend's
 * shader-module call needs, so `VulkanEngine`/`WebGpuEngine` never branch on which variant
 * they got. */
sealed interface ShaderSource {
    data class ResourcePath(
        val path: String,
        val entryPoint: String = "main",
    ) : ShaderSource

    class PrecompiledBinary(
        val bytes: ByteArray,
        val entryPoint: String = "main",
    ) : ShaderSource

    data class InlineText(val sourceCode: String) : ShaderSource // Great for WebGPU/WGSL
}

/** [ShaderSource]'s own entry point, regardless of variant -- callers building a
 * `ShaderStageResource`-equivalent no longer need a `when` per variant just to read this. */
val ShaderSource.entryPoint: String
    get() = when (this) {
        is ShaderSource.ResourcePath -> entryPoint
        is ShaderSource.PrecompiledBinary -> entryPoint
        is ShaderSource.InlineText -> "main"
    }

/** Resolves [this] to raw bytes -- [ShaderSource.ResourcePath] reads from disk/assets via
 * [readResourceBytes] (hence `suspend`), the other two variants already have bytes in hand. */
suspend fun ShaderSource.resolveBytes(): ByteArray = when (this) {
    is ShaderSource.ResourcePath -> readResourceBytes(path)
    is ShaderSource.PrecompiledBinary -> bytes
    is ShaderSource.InlineText -> sourceCode.encodeToByteArray()
}

/** One backend's shader stages, keyed by [ShaderStage] -- [graphics] is the shape every real
 * pipeline in this engine uses today (vertex + fragment); [compute] exists for a future compute
 * pipeline (none exist yet, see this module's own doc comment) but costs nothing to keep. */
class ShaderStages private constructor(
    val stages: Map<ShaderStage, ShaderSource>,
) {
    operator fun get(stage: ShaderStage): ShaderSource? = stages[stage]

    companion object {
        fun graphics(vertex: ShaderSource, fragment: ShaderSource) = ShaderStages(
            mapOf(ShaderStage.VERTEX to vertex, ShaderStage.FRAGMENT to fragment),
        )

        fun compute(compute: ShaderSource) = ShaderStages(
            mapOf(ShaderStage.COMPUTE to compute),
        )
    }
}

/** A shader program described once per backend -- [vulkan]/[webGpu] each resolve independently
 * (Vulkan wants separate `.vert.spv`/`.frag.spv` files, WebGPU wants one `.wgsl` file read for
 * both stages), so a caller building an `AwakeApplication` for either backend just reads its own
 * half; the other is never touched. */
data class ShaderSet(
    val vulkan: ShaderStages,
    val webGpu: ShaderStages,
)

/** Escape hatch for a shader whose two backends genuinely need different stage sources (not just
 * different file paths under the same naming convention -- see [shaderSet] below for that common
 * case). */
fun shaderSet(
    vulkan: ShaderStages,
    webGpu: ShaderStages,
): ShaderSet = ShaderSet(
    vulkan = vulkan,
    webGpu = webGpu,
)

/** One [directory]/[name]-derived [ShaderStages] -- [vertexFile]/[fragmentFile] are the same
 * string for a backend whose stages share one file (WebGPU's `.wgsl`), different strings for a
 * backend with separate per-stage files (Vulkan's `.vert.spv`/`.frag.spv`). The only thing that
 * varies per backend in [shaderSet] below is these file names -- everything else (the
 * `ResourcePath`/entry-point shape) is written here exactly once. */
private fun conventionStages(directory: String, vertexFile: String, fragmentFile: String): ShaderStages =
    ShaderStages.graphics(
        vertex = ShaderSource.ResourcePath("$directory/$vertexFile", entryPoint = "vertexMain"),
        fragment = ShaderSource.ResourcePath("$directory/$fragmentFile", entryPoint = "fragmentMain"),
    )

/** The common case: a shader named [name] follows this engine's file-naming convention on both
 * backends, so a caller just names it once -- see `docs/reference/...` for the convention
 * (`assets/shader/vulkan/$name.vert.spv`+`.frag.spv`, `assets/shader/webgpu/$name.wgsl`). */
fun shaderSet(
    name: String,
    vulkanDirectory: String = "assets/shader/vulkan",
    webGpuDirectory: String = "assets/shader/webgpu",
): ShaderSet = ShaderSet(
    vulkan = conventionStages(vulkanDirectory, "$name.vert.spv", "$name.frag.spv"),
    webGpu = conventionStages(webGpuDirectory, "$name.wgsl", "$name.wgsl"),
)
