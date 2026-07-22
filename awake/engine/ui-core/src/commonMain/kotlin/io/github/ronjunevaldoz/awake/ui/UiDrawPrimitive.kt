// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

/**
 * Backend-neutral output of a [io.github.ronjunevaldoz.awake.ui.context.UiContext] frame -- each backend's `Renderer.drawUi` converts
 * these into its own dynamic vertex/index buffer. Pixel-space coordinates (screen-space,
 * Y-down), not NDC -- the NDC transform is the shader's job (see `ui_quad.vert`/`.wgsl`).
 */
sealed class UiDrawPrimitive {
    data class Quad(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val color: Color
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Quad) return false
            return x == other.x && y == other.y && w == other.w && h == other.h &&
                color == other.color
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + w.hashCode()
            result = 31 * result + h.hashCode()
            result = 31 * result + color.hashCode()
            return result
        }
    }

    data class GradientQuad(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val gradient: UiLinearGradient
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GradientQuad) return false
            return x == other.x && y == other.y && w == other.w && h == other.h &&
                gradient == other.gradient
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + w.hashCode()
            result = 31 * result + h.hashCode()
            result = 31 * result + gradient.hashCode()
            return result
        }
    }

    /** Rounded-corner sibling of [Quad] -- kept as a separate type rather than a `radius`
     * field on [Quad] so the hot-path flat rect every existing widget already emits every
     * frame never pays a corner-test cost (kool-engine's own `RectBackground` vs
     * `RoundRectBackground` split backs this). A backend that doesn't special-case this yet
     * may fall back to drawing it as a flat [Quad] (ignore [radius]) -- see this repo's UI
     * architecture review doc for the shader work needed to actually render it rounded. */
    data class RoundedQuad(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val color: Color,
        val radius: Float
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is RoundedQuad) return false
            return x == other.x && y == other.y && w == other.w && h == other.h &&
                radius == other.radius && color == other.color
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + w.hashCode()
            result = 31 * result + h.hashCode()
            result = 31 * result + radius.hashCode()
            result = 31 * result + color.hashCode()
            return result
        }
    }

    /** One glyph quad sampling a [io.github.ronjunevaldoz.awake.ui.font.BitmapFont]'s atlas
     * -- Phase B (see docs/MVP_PLAN.md's custom-UI decision log), drawn via a second,
     * textured pipeline after [Quad]s in the same UI overlay pass. */
    data class Glyph(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val u0: Float,
        val v0: Float,
        val u1: Float,
        val v1: Float,
        val color: Color
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Glyph) return false
            return x == other.x && y == other.y && w == other.w && h == other.h &&
                u0 == other.u0 && v0 == other.v0 && u1 == other.u1 && v1 == other.v1 &&
                color == other.color
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + w.hashCode()
            result = 31 * result + h.hashCode()
            result = 31 * result + u0.hashCode()
            result = 31 * result + v0.hashCode()
            result = 31 * result + u1.hashCode()
            result = 31 * result + v1.hashCode()
            result = 31 * result + color.hashCode()
            return result
        }
    }

    /** Renderer-neutral filled shape primitive. Backends without real path support may
     * conservatively fall back to the path's bounds rect until dedicated tessellation or
     * shader support lands. */
    data class FilledPath(
        val path: UiPath,
        val color: Color
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FilledPath) return false
            return path == other.path && color == other.color
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + color.hashCode()
            return result
        }
    }

    /** Stroke sibling of [FilledPath]. [stroke] stays in dp-space so backends can convert it
     * with the same density contract as every other UI size. */
    data class StrokedPath(
        val path: UiPath,
        val stroke: UiStroke,
        val color: Color
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StrokedPath) return false
            return path == other.path && stroke == other.stroke && color == other.color
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + stroke.hashCode()
            result = 31 * result + color.hashCode()
            return result
        }
    }

    /** One screen-space quad sampling an arbitrary render-target-backed material -- e.g. a
     * minimap or portal-camera preview composited into the UI overlay. [material] is typed
     * as [Any] rather than `awake-engine-render-api`'s `Material` interface: THAT module
     * already depends on this one (`Renderer.drawUi(primitives: List<UiDrawPrimitive>, ...)`),
     * so a `Material` reference here would create a module dependency cycle. [material] is
     * expected to be whatever `Renderer.createMaterial(renderTarget = ...)` returned;
     * `UiContext` never inspects it, just carries it back to the backend's own `Renderer
     * .drawUi()`, which casts it to its own concrete `Material` type -- the same "opaque
     * handle round-tripped back to the one place that knows its real type" pattern
     * `DrawCall.mesh`/`.material` already use across the render-api/backend boundary.
     * Unlike [Glyph] (which always samples the one fixed font atlas), each [Texture]
     * primitive carries its own [material], so a backend's UI pass must bind a DIFFERENT
     * sampled image per primitive instead of one baked in at pipeline-construction time. */
    data class Texture(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val material: Any
    ) : UiDrawPrimitive()

    /** Path-based clip sibling of [ClipPush]. [boundsRect] is already intersected against
     * the active clip stack, so backends without stencil/mask support can still conservatively
     * fall back to plain scissor clipping on that rect. Consumers that do understand shape
     * clipping can use [path] for the exact mask. */
    data class ClipPathPush(
        val path: UiPath,
        val boundsRect: UiSlot
    ) : UiDrawPrimitive()

    /** Marks the start of a clipped region -- [rect] is always already-intersected against
     * whatever clip was active before it ([io.github.ronjunevaldoz.awake.ui.context.UiContext]'s clip stack resolves nesting, never
     * the backend), so every backend just naively "sets scissor to this rect," identical
     * logic on every platform. Not a real draw call -- carries no vertices, just tells the
     * backend's command-buffer recording where to issue a scissor-rect change. */
    data class ClipPush(val rect: UiSlot) : UiDrawPrimitive()

    /** Restores the scissor rect that was active before the matching [ClipPush] -- [restoreRect]
     * is resolved by [io.github.ronjunevaldoz.awake.ui.context.UiContext] at pop time (the next rect down the clip stack, or the full
     * frame extent if the stack is now empty), so the backend needs no stack-awareness here
     * either. */
    data class ClipPop(val restoreRect: UiSlot) : UiDrawPrimitive()
}
