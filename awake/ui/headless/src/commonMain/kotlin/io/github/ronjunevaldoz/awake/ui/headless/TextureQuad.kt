// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.textureQuad as primitiveTextureQuad

/**
 * Draws an offscreen render target into the layout slot [modifier] claims.
 *
 * [material] is an opaque backend handle (`Renderer.createMaterial(renderTarget = ...)`) that this
 * layer only forwards -- Headless has no render-backend dependency and never inspects it.
 */
fun UiScope.textureQuad(material: Any, modifier: UiModifier = Modifier) {
    primitive.primitiveTextureQuad(material = material, modifier = modifier)
}
