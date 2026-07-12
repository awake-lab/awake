// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
/** Saves tightly-packed RGBA8 [pixels] ([width]x[height]) as a PNG at [path], for visually
 * confirming an offscreen `RenderTarget`'s contents (`DemoCatalog`'s offscreen-readback
 * sample test) -- a debugging convenience, not an engine feature, so it's a plain top-level
 * function here rather than something on `Renderer`/`RenderTarget` themselves. Only
 * implemented where a real filesystem + image-encoding library are trivially available
 * (desktop JVM's `javax.imageio`); other platforms log instead of writing a file. */
expect fun saveDebugPng(pixels: ByteArray, width: Int, height: Int, path: String)
