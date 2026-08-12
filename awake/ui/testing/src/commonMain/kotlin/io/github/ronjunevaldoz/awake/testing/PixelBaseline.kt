// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing

/** Result of comparing a rendered frame's RGBA8 bytes against a golden baseline -- see
 * [comparePixels]. [matches] is what a test asserts on; the rest is diagnostic detail for a
 * failure message. */
data class PixelDiffResult(
    val matches: Boolean,
    val diffPixelCount: Int,
    val maxChannelDiff: Int,
)

/**
 * Compares two tightly-packed RGBA8 byte buffers (same layout `Renderer.readPixels`/
 * `TextureAsset.data` already produce on every backend -- Vulkan and WebGPU alike, since
 * both implement the same `Renderer` interface) pixel-by-pixel, allowing up to
 * [toleranceParChannel] difference per color channel before a pixel counts as mismatched.
 *
 * Deliberately pure/common-code -- no file I/O, no test-framework dependency (`kotlin.test`
 * stays a `commonTest`-only concern here) -- so a consumer's own test can call this the same
 * way on desktop, Android, iOS, or wasmJs after loading baseline bytes however that platform
 * prefers (e.g. `awake:base`'s `readResourceBytes`).
 *
 * A tolerance above zero exists because GPU rasterization/blending can legitimately differ
 * by a channel value or two between backends (Vulkan vs WebGPU) or driver versions, without
 * that being a real regression -- an exact-bytes comparison would be too brittle to be
 * useful across this repo's 5 targets.
 */
fun comparePixels(actual: ByteArray, expected: ByteArray, toleranceParChannel: Int = 2): PixelDiffResult {
    require(actual.size == expected.size) {
        "Pixel buffer size mismatch: actual=${actual.size} bytes, expected=${expected.size} bytes " +
            "(likely a width/height mismatch between the rendered frame and the baseline)"
    }
    var diffPixelCount = 0
    var maxChannelDiff = 0
    var offset = 0
    while (offset < actual.size) {
        var pixelDiffers = false
        var channel = 0
        while (channel < 4) {
            val diff = kotlin.math.abs(
                (actual[offset + channel].toInt() and 0xFF) - (expected[offset + channel].toInt() and 0xFF),
            )
            if (diff > maxChannelDiff) maxChannelDiff = diff
            if (diff > toleranceParChannel) pixelDiffers = true
            channel += 1
        }
        if (pixelDiffers) diffPixelCount += 1
        offset += 4
    }
    return PixelDiffResult(matches = diffPixelCount == 0, diffPixelCount = diffPixelCount, maxChannelDiff = maxChannelDiff)
}
