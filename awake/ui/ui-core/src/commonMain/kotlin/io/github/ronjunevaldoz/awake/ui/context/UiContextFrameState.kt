// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.intersect

internal class UiContextFrameState {
    private val renderCollector = UiRenderCollector()
    private val semanticCollector = UiSemanticCollector()

    // Two independent clip stacks, not one shared stack keyed by call order -- see [pushClip]'s
    // doc comment for why. clip() dispatch is synchronous/nested regardless of which bucket a
    // widget's primitives end up in, so a single shared stack would let an ambient non-overlay
    // ancestor's (possibly much smaller, e.g. a WrapContent card sized only around its normal-
    // flow content) clip rect leak into a nested overlay widget's (e.g. a dialog/popup/tooltip)
    // *own* clip intersection just because the overlay widget's composition happens to run
    // textually nested inside that ancestor's content lambda.
    private val clipStack = ArrayList<UiBounds>()
    private val overlayClipStack = ArrayList<UiBounds>()

    // Every id-bearing widget in this codebase -- surface()/interactiveSurface() (hence
    // avatar/separator/every shadcn* recipe built on them), column(), checkbox()/radio(),
    // textField()/textarea(), lazyColumn()/lazyRow(), resizablePanelGroup's panel()/handle(),
    // toast/progressBar/skeleton/switch/toggle/slider/rangeSlider/dropdown/canvas/text() -- calls
    // recordSemantic(id = ...) itself, directly or through exactly one shared internal choke
    // point, exactly ONCE per real (non-measuring) render of that widget instance. Unlike
    // widgetState(id), which the same instance can legitimately re-enter from more than one
    // internal helper in one frame (see TextField.kt's cursorState/caretBlinkElapsedSeconds), no
    // such multi-call pattern exists for recordSemantic anywhere in ui-core/headless/designsystem
    // -- so a same-id-twice-in-one-frame hit here really is two sibling instances colliding on
    // the same literal id, not one instance's own repeat call. See
    // docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md's P5 row.
    private val claimedSemanticIdsThisFrame = HashSet<String>()

    var inputState: UiInputState = UiInputState()
        private set
    var fullFrameRect: UiBounds = UiBounds(0f, 0f, 0f, 0f)
        private set
    var frameDeltaSeconds: Float = 1f / 60f
        private set

    fun beginFrame(screenWidth: Float, screenHeight: Float, inputState: UiInputState, deltaSeconds: Float) {
        renderCollector.beginFrame()
        semanticCollector.beginFrame()
        clipStack.clear()
        overlayClipStack.clear()
        claimedSemanticIdsThisFrame.clear()
        fullFrameRect = UiBounds(0f, 0f, screenWidth, screenHeight)
        frameDeltaSeconds = deltaSeconds.coerceAtLeast(0f)
        this.inputState = inputState
    }

    fun endFrame(): List<UiDrawPrimitive> = renderCollector.endFrame()

    fun emit(primitive: UiDrawPrimitive) =
        renderCollector.emit(primitive)

    fun emitOverlay(primitive: UiDrawPrimitive) =
        renderCollector.emitOverlay(primitive)

    fun recordSemantic(node: UiSemanticNode) {
        val id = node.id
        if (id != null && !claimedSemanticIdsThisFrame.add(id)) {
            error(
                "Duplicate widget id '$id' claimed by two sibling widgets in the same frame -- " +
                    "widget ids must be unique per screen. Pass a distinct literal id (or a " +
                    "derived one, e.g. \"\$id.\${index}\" inside a loop) to each caller.",
            )
        }
        semanticCollector.record(node)
    }

    fun semanticNodes(): List<UiSemanticNode> = semanticCollector.snapshot()

    /**
     * Resolves [rect] against the clip stack for [overlay]'s bucket only -- the overlay bucket
     * (dialogs/popups/tooltips/dropdowns, painted last regardless of call order, see
     * [emitOverlay]) is always visually independent of whatever's currently on the *normal*
     * bucket's clip stack, even though its content() is dispatched synchronously nested inside
     * a normal-bucket ancestor's own composition. Before this split, a WrapContent card with
     * rounded corners (hence an auto-clip -- see Surface.kt) sized only around its own normal-
     * flow content (a popup's real height never contributes to that measurement -- it's an
     * overlay) would bake that undersized rect into every nested overlay widget's *own* clip via
     * the old single shared stack's `current.intersect(rect)`, permanently truncating dialog/
     * popup content no matter how correctly that widget's own wrap-height was measured.
     */
    fun pushClip(rect: UiBounds, overlay: Boolean = false): UiBounds {
        val stack = if (overlay) overlayClipStack else clipStack
        val current = stack.lastOrNull() ?: fullFrameRect
        val resolved = current.intersect(rect)
        stack += resolved
        return resolved
    }

    fun popClip(overlay: Boolean = false): UiBounds {
        val stack = if (overlay) overlayClipStack else clipStack
        if (stack.isNotEmpty()) stack.removeAt(stack.size - 1)
        return stack.lastOrNull() ?: fullFrameRect
    }
}
