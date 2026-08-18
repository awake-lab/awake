// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.systems

import io.github.ronjunevaldoz.awake.ecs.Entity

/** Holds the one persistent editor (Scene-view) camera entity -- created once in `onReady`,
 * survives every `LoadExample` (unlike a scene's own authored camera, which is recreated on
 * every load). Threaded from `studioModule()`'s `onReady` block (which creates the entity) into
 * [StudioExampleDriverSystem] (which drives it and flips `Camera.isPrimary` by `StudioContract
 * .Mode`) -- these run at different times relative to `gameModule { }` construction, so a plain
 * `val Entity` can't be shared between them directly. */
internal class StudioEditorCamera {
    var entity: Entity? = null

    /** The scene's own authored camera entity, last seen -- identity changing means a new
     * example just loaded (its camera entity is recreated by `ExampleLoader` every
     * `LoadExample`), and the editor camera's orbit should re-seed from its pose. */
    var authoredCameraEntity: Entity? = null
}
