// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.render.renderer.DrawCall

/**
 * Optional per-demo contribution to [DemoCatalog]'s offscreen-readback sample test -- a demo
 * (e.g. [CubeDemo]) implements this to expose its own geometry for a `renderToTexture` call
 * to draw, so that test proves real scene rendering into a render target instead of just a
 * clear color. Mirrors [DebugReadout]'s shape/rationale: not part of the `Game` interface
 * itself, most demos won't need it, purely a sample-hello-cube debugging convenience.
 */
interface OffscreenPreviewSource {
    /** Draw calls to render into the offscreen preview target this frame -- keep this
     * cheap, same as [DebugReadout.debugLines]. */
    fun sampleDrawCalls(): List<DrawCall>
}
