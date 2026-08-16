// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.gizmo

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/**
 * Where the viewport panel was last laid out, shared between the UI pass that measures it and the
 * frame system that hit-tests against it.
 *
 * Not read back from `Renderer.sceneViewport`, even though the shell sets that to the same rect:
 * those accessors default to ignoring the value, so any renderer that has not opted in -- every
 * test double included -- reads back `null` and would silently disable picking rather than fail.
 * Studio owns the rect; the renderer is just another consumer of it.
 *
 * The UI's layout runs several passes per frame, so this is written more than once; the last
 * write is the placed rect, and the frame system reads it after the whole UI pass is done.
 */
class StudioViewportRect {
    var bounds: UiBounds? = null
}
