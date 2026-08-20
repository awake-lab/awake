// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

/** Which side of a mesh's triangles a backend's rasterizer discards, per [DrawCall]/
 * `MeshRenderer` -- [None] (the default, and every mesh's behavior before this existed) draws
 * both sides always, correct for anything genuinely double-sided (a leaf, a flat pane) but wastes
 * roughly half the fragment work on a solid opaque mesh and can z-fight against itself when
 * viewed from the inside/underneath (its own back faces competing with its front faces for the
 * same depth). [Back] is the normal choice for a solid, correctly-wound opaque mesh -- discards
 * the faces pointing away from the camera, fixing both the wasted work and the self-z-fight.
 * [Front] exists for completeness (a caller drawing intentionally inside-out, e.g. a skybox-style
 * inverted cube) but nothing in this engine uses it today. */
enum class CullMode {
    None,
    Back,
    Front,
}
