// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/** 16:9, small enough that a per-frame offscreen pass stays cheap next to the main one. */
private const val PREVIEW_WIDTH = 320
private const val PREVIEW_HEIGHT = 180

/**
 * The Inspector's live thumbnail of what a selected in-scene camera sees.
 *
 * One target/material pair for the whole session, created on the first preview and reused for
 * every camera afterwards: the thumbnail's size never changes, so a selection change only changes
 * which camera renders into it. A pair per selected entity would allocate GPU memory at selection
 * rate -- and studio's UI is immediate mode, so "selection changed" is only ever observed by
 * comparing against remembered state, which is the dispose-order trap this shape avoids entirely.
 *
 * Created once by `studioModule` and threaded through the shell, like [io.github.ronjunevaldoz
 * .awake.studio.gizmo.StudioGizmo] -- the draw functions themselves run every frame and can own
 * nothing that needs destroying.
 */
internal class StudioCameraPreview {
    private var target: RenderTarget? = null

    /** The texture to composite, or `null` until [render] has run once. */
    var material: Material? = null
        private set

    /**
     * Renders [camera]'s view of [drawCalls] into the preview target.
     *
     * Called once per frame from the shell rather than from the inspector's own draw: a resizable
     * group measures its content more than once per frame, and an offscreen pass per measure pass
     * would stall the frame repeatedly for identical pixels.
     */
    fun render(renderer: Renderer, camera: CoreCamera, drawCalls: List<DrawCall>) {
        val renderTarget = target ?: renderer.createRenderTarget(PREVIEW_WIDTH, PREVIEW_HEIGHT).also { target = it }
        if (material == null) material = renderer.createMaterial(renderTarget = renderTarget)
        // The shell points sceneViewport at the viewport panel, in window coordinates, and that
        // rect applies to renderToTexture too (see RendererSceneViewportTest) -- left set, it
        // confines this pass to whatever slice of a 320x180 target those coordinates clamp to.
        val sceneViewport = renderer.sceneViewport
        renderer.sceneViewport = null
        renderer.renderToTexture(renderTarget, camera, drawCalls)
        renderer.sceneViewport = sceneViewport
    }

    /**
     * Destroys the material.
     *
     * The render target is deliberately not destroyed here: the renderer tracks every target it
     * creates and destroys them in its own `destroy()`, so destroying it here as well would
     * double-destroy the same GPU images. Materials are not tracked, so this one is ours.
     */
    fun dispose() {
        material?.destroy()
        material = null
        target = null
    }
}
