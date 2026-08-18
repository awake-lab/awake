// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.WidgetState
import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.scaledByAlpha
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.withTransform

/**
 * Minimal immediate-mode UI context -- ImGui's own architecture (hot/active id tracking, no
 * retained widget tree, no ECS entities). Pure layout and spatial hit-testing engine.
 */
class UiContext internal constructor(
    private val measuring: Boolean = false,
    private val stateStore: UiStateStore = UiStateStore(),
) {
    constructor() : this(measuring = false)

    private val stacks = UiContextStacks()
    private val runtime = UiRuntimeCoordinator(stateStore = stateStore)
    private val measurement = UiMeasurementRuntime()
    private val layouts = UiLayoutFactory(this)

    val currentShapeSpec get() = stacks.currentShapeSpec
    val inputState: UiInputState get() = runtime.inputState

    /** Reads an app- or engine-declared [UiLocal] at this point in the tree. */
    fun <T> current(local: UiLocal<T>): T = stacks.current(local)

    /** Prefer `Provide(local, value) { }`, which cannot leak the value past a throw. */
    fun <T> pushLocal(local: UiLocal<T>, value: T) = stacks.push(local, value)
    fun <T> popLocal(local: UiLocal<T>) = stacks.pop(local)

    fun pushShapeSpec(spec: io.github.ronjunevaldoz.awake.ui.UiShapeSpec?) = stacks.pushShapeSpec(spec)
    fun popShapeSpec() = stacks.popShapeSpec()

    /** Internal ambient-state handoff for a cached measurement context. */
    internal fun ambientSnapshotInternal(): UiLocalSnapshot = stacks.snapshot()
    internal fun restoreAmbientSnapshotInternal(snapshot: UiLocalSnapshot) = stacks.restore(snapshot)

    /**
     * Resets the context for a new frame. Accepts [UiInputState] to remain
     * decoupled from hardware input modules.
     */
    fun beginFrame(frame: UiFrameInput) {
        runtime.beginFrame(
            screenWidth = frame.viewportWidth,
            screenHeight = frame.viewportHeight,
            inputState = frame.input,
            deltaSeconds = frame.deltaSeconds,
        )
        measurement.beginFrame()
        beginWeightAnswerFrame()
    }

    fun createColumn(
        x: Float,
        y: Float,
        width: Float,
        height: Float? = null,
        verticalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = height != null,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiBounds>? = null,
        horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    ): ColumnScope = layouts.createColumn(
        x = x,
        y = y,
        width = width,
        height = height,
        verticalArrangement = verticalArrangement,
        tracking = UiLayoutTracking(testTag, hasBoundedFillWidth, hasBoundedFillHeight, overlayOnly),
        plannedSlots = plannedSlots,
        horizontalAlignment = horizontalAlignment,
    )

    fun createColumn(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        verticalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiBounds>? = null,
        horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    ): ColumnScope = layouts.createColumn(
        slot = slot,
        insets = insets,
        verticalArrangement = verticalArrangement,
        tracking = UiLayoutTracking(testTag, hasBoundedFillWidth, hasBoundedFillHeight, overlayOnly),
        plannedSlots = plannedSlots,
        horizontalAlignment = horizontalAlignment,
    )

    fun createAbsolute(
        x: Float,
        y: Float,
        testTag: String? = null,
        overlayOnly: Boolean = false,
    ): AbsoluteScope = layouts.createAbsolute(x, y, testTag, overlayOnly)

    fun createAbsolute(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        testTag: String? = null,
        overlayOnly: Boolean = false,
    ): AbsoluteScope = layouts.createAbsolute(slot, insets, testTag, overlayOnly)

    fun createRow(
        x: Float,
        y: Float,
        height: Float,
        width: Float? = null,
        horizontalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = width != null,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiBounds>? = null,
        verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    ): RowScope = layouts.createRow(
        x = x,
        y = y,
        height = height,
        width = width,
        horizontalArrangement = horizontalArrangement,
        tracking = UiLayoutTracking(testTag, hasBoundedFillWidth, hasBoundedFillHeight, overlayOnly),
        plannedSlots = plannedSlots,
        verticalAlignment = verticalAlignment,
    )

    fun createRow(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        horizontalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiBounds>? = null,
        verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    ): RowScope = layouts.createRow(
        slot = slot,
        insets = insets,
        horizontalArrangement = horizontalArrangement,
        tracking = UiLayoutTracking(testTag, hasBoundedFillWidth, hasBoundedFillHeight, overlayOnly),
        plannedSlots = plannedSlots,
        verticalAlignment = verticalAlignment,
    )

    fun createBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
    ): BoxScope = layouts.createBox(
        x = x,
        y = y,
        width = width,
        height = height,
        contentAlignment = contentAlignment,
        tracking = UiLayoutTracking(testTag, hasBoundedFillWidth, hasBoundedFillHeight, overlayOnly),
    )

    fun createBox(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
    ): BoxScope = layouts.createBox(
        slot = slot,
        insets = insets,
        contentAlignment = contentAlignment,
        tracking = UiLayoutTracking(testTag, hasBoundedFillWidth, hasBoundedFillHeight, overlayOnly),
    )

    fun columnAt(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        verticalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        content: ColumnScope.() -> Unit,
    ) {
        createColumn(
            slot = slot,
            insets = insets,
            verticalArrangement = verticalArrangement,
            testTag = testTag,
        ).content()
    }

    fun rowAt(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        horizontalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        content: RowScope.() -> Unit,
    ) {
        createRow(
            slot = slot,
            insets = insets,
            horizontalArrangement = horizontalArrangement,
            testTag = testTag,
        ).content()
    }

    fun box(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        testTag: String? = null,
        content: BoxScope.() -> Unit,
    ) {
        createBox(
            slot = slot,
            insets = insets,
            contentAlignment = contentAlignment,
            testTag = testTag,
        ).content()
    }

    fun absolute(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        testTag: String? = null,
        content: AbsoluteScope.() -> Unit,
    ) {
        createAbsolute(
            slot = slot,
            insets = insets,
            testTag = testTag,
        ).content()
    }

    fun finishFrame(): UiFrameOutput = runtime.finishFrame()

    internal fun onOverScrollableInternal() {
        if (!measuring) runtime.onOverScrollable()
    }

    internal fun onScrollConsumedInternal() {
        if (!measuring) runtime.onScrollConsumed()
    }

    internal fun hitTestInternal(slot: UiBounds): Boolean =
        !measuring && runtime.hitTest(slot)

    internal fun isActiveInternal(id: String): Boolean = runtime.isActive(id)

    internal fun tryClaimActiveInternal(id: String, hovered: Boolean) {
        if (!measuring) runtime.tryClaimActive(id, hovered)
    }

    internal fun releaseActiveIfMatchesInternal(id: String) {
        if (!measuring) runtime.releaseActiveIfMatches(id)
    }

    // Public (not module-internal) despite the name: a handful of low-level test harnesses hold
    // a raw UiContext with no UiPrimitiveScope to route through -- see
    // io.github.ronjunevaldoz.awake.ui.scope.isFocused for the scope-based path ordinary widget
    // code should prefer.
    fun isFocusedInternal(id: String): Boolean = runtime.isFocused(id)

    internal fun requestFocusInternal(id: String) {
        if (!measuring) runtime.requestFocus(id)
    }

    internal fun clearFocusIfMatchesInternal(id: String) {
        if (!measuring) runtime.clearFocusIfMatches(id)
    }

    /** The composed alpha of every active [io.github.ronjunevaldoz.awake.ui.modifier.UiAlphaEffect]
     * layer (see [pushGraphicsLayerAlphaInternal]) -- `1f` (fully opaque) when none is active. */
    val currentGraphicsLayerAlpha: Float get() = stacks.currentAlpha

    /**
     * Pushes [alpha] onto the graphics-layer alpha stack, multiplying with whatever alpha is
     * already active (so nested layers compose the same way real compositing does) -- every
     * primitive emitted via [emitInternal]/[emitOverlayInternal] anywhere inside the matching
     * [popGraphicsLayerAlphaInternal] window, no matter how many nested composite widgets deep,
     * has its color's alpha channel multiplied by the effective stacked value (see
     * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.scaledByAlpha]). This is the real
     * application point for [io.github.ronjunevaldoz.awake.ui.modifier.UiAlphaEffect] -- see
     * `UiAnimatedVisibility.kt`'s `withGraphicsLayerAlpha`/`animatedVisibility` for the public
     * scoped-block API built on top of this pair.
     */
    fun pushGraphicsLayerAlphaInternal(alpha: Float) = stacks.pushAlpha(alpha)

    fun popGraphicsLayerAlphaInternal() = stacks.popAlpha()

    /** The innermost active [io.github.ronjunevaldoz.awake.ui.modifier.UiScaleEffect]'s
     * transform (see [pushGraphicsLayerScaleInternal]), or `null` if none is active. */
    val currentGraphicsLayerTransform: UiPrimitiveTransform? get() = stacks.currentTransform

    /**
     * Pushes [transform] onto the graphics-layer scale stack -- every geometry-carrying
     * primitive ([io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Quad]/`RoundedQuad`/`Glyph`/
     * `Texture`) emitted via [emitInternal]/[emitOverlayInternal] anywhere inside the matching
     * [popGraphicsLayerScaleInternal] window carries [transform] through to the backend's draw
     * call (see [io.github.ronjunevaldoz.awake.ui.withTransform]). Unlike alpha, nested scale
     * blocks do NOT compose multiplicatively (see [UiContextStacks.pushTransform]'s doc) -- the
     * real application point for [io.github.ronjunevaldoz.awake.ui.modifier.UiScaleEffect], see
     * `UiGraphicsLayerScope.kt`'s `withGraphicsLayerScale` for the public scoped-block API.
     */
    fun pushGraphicsLayerScaleInternal(transform: UiPrimitiveTransform) =
        stacks.pushTransform(transform)

    fun popGraphicsLayerScaleInternal() = stacks.popTransform()

    fun emitInternal(p: UiDrawPrimitive) {
        if (!measuring) runtime.emit(p.scaledByAlpha(stacks.currentAlpha).withTransform(stacks.currentTransform))
    }

    fun emitOverlayInternal(p: UiDrawPrimitive) {
        if (!measuring) runtime.emitOverlay(p.scaledByAlpha(stacks.currentAlpha).withTransform(stacks.currentTransform))
    }

    fun widgetStateInternal(id: String): WidgetState = runtime.widgetState(id)

    fun recordSemanticInternal(node: UiSemanticNode) {
        if (!measuring) runtime.recordSemantic(node)
    }

    fun pushClipInternal(rect: UiBounds, overlay: Boolean = false): UiBounds = runtime.pushClip(rect, overlay)

    fun popClipInternal(overlay: Boolean = false): UiBounds = runtime.popClip(overlay)

    fun pointerDownEdgeInternal(): Boolean = runtime.pointerDownEdge()

    fun setActiveInternal(id: String?) {
        runtime.setActive(id)
    }

    /** Gated by `!measuring` like [tryClaimActiveInternal]/[requestFocusInternal] -- a trial-
     * measure pass (e.g. [io.github.ronjunevaldoz.awake.ui.scope.measureColumnContent]) re-runs
     * widget code to size a WrapContent container and must not let that dry run's hover state
     * leak a cursor request into the real frame's [UiFrameOutput.effects]. */
    fun requestCursorInternal(cursor: UiCursor) {
        if (!measuring) runtime.requestCursor(cursor)
    }

    fun frameDeltaSecondsInternal(): Float = runtime.frameDeltaSeconds

    // Public (not module-internal) despite the name -- see isFocusedInternal's doc above; the
    // same low-level, scope-less test harnesses read frame bounds off a raw UiContext.
    fun frameBoundsInternal(): UiBounds = runtime.fullFrameRect

    fun isMeasuringInternal(): Boolean = measuring

    // Set only around a WrapContent container's own sizing trial (resolveMeasuredColumn's/
    // resolveMeasuredRow's measureColumnContentInternal/measureRowContentInternal call, via
    // wrapContentPass below) -- NOT the separate hasWeightedChild-detection or plannedSlots
    // trials that also run through this same trial-context machinery, which measure against a
    // real, final bound and must not have a child's reported size swapped out from under them.
    // A FillMax child has no real intrinsic size of its own to place, but during THIS specific
    // trial it may still have a natural content size worth reporting upward (e.g. a button's
    // label width) -- see withIntrinsicLabelWidth, the one place that currently reads this.
    private var wrapContentPass = false

    // Not module-internal despite the name -- like isFocusedInternal above, widget-layer code
    // in other modules (e.g. headless's withIntrinsicLabelWidth) reads this off a raw UiContext.
    fun isWrapContentPassInternal(): Boolean = wrapContentPass

    /** Brackets [block] as this context's own WrapContent sizing trial -- see [wrapContentPass].
     * Restores the previous value rather than unconditionally clearing it, since a trial
     * context is reused across nested/sequential calls (see
     * [UiContextMeasureState.createMeasureContext]) and one trial's content can itself trigger
     * another (a nested WrapContent child). */
    internal inline fun <T> withWrapContentPass(value: Boolean, block: () -> T): T {
        val previous = wrapContentPass
        wrapContentPass = value
        try {
            return block()
        } finally {
            wrapContentPass = previous
        }
    }

    /** The real, persisted widget-state store this context reads/writes through
     * [rememberStateValue][io.github.ronjunevaldoz.awake.ui.rememberStateValue] et al. --
     * exposed so a trial/measurement [UiContext] (see [UiContextMeasureState]) can share it
     * with its source context instead of measuring stateful content (a `WrapContent` branch
     * that reads persisted selection/toggle state, for example) against fresh default values. */
    internal fun stateStoreInternal(): UiStateStore = stateStore

    // A row/column's own "measured" trial (see measureRowContentInternal/measureColumnContentInternal
    // below) reads measuredSlots/measuredWeights as one entry per DIRECT child claim, index-
    // paired (resolveWeightedMainAxis() zips measured.slots[i] with measured.weights[i] as "the
    // same child"). But claimSlot() (RowScope/ColumnScope/BoxScope/AbsoluteScope alike) always
    // calls context.recordMeasuredSlot()/recordMeasuredWeight() whenever this context is
    // measuring -- and once a DIRECT child is itself composite (e.g. a column() wrapping its
    // own label/input surfaces), that composite child's *own* real render pass (its
    // `scope.content(slot)` tail call) reuses this very same context/measurement pair, so its
    // grandchildren's claims would land in the same flat measuredSlots/measuredWeights lists as
    // the row's direct children, corrupting that index pairing. recordingSuppressionDepth is a
    // bracket around exactly that "rendering a composite child's own children" window (see
    // UiPrimitiveScope.row()/UiPrimitiveScope.column()) -- it only excludes claims from measuredSlots/
    // measuredWeights (via contributesToChildList below), NOT from measuredMaxRight/
    // measuredMaxBottom (see UiContextMeasureState.record()), which WrapContent width/height
    // resolution intentionally keeps hugging across the *whole* subtree (e.g. a WrapContent
    // shadcnCard sizing itself around a nested row's own buttons).
    private var recordingSuppressionDepth = 0

    // Unlike recordingSuppressionDepth above (which deliberately still lets a composite child's
    // grandchildren dictate an ancestor's WrapContent hugging -- e.g. a card sizing around a
    // nested row's buttons), a *scroll* viewport's children are a different case entirely: they
    // are deliberately laid out beyond the viewport's own bounds (that's the whole point of
    // scrolling) and positioned using the current, ephemeral scroll offset. If those claims were
    // allowed to reach measuredMaxRight/measuredMaxBottom during a WrapContent ancestor's own
    // trial-measurement pass, that ancestor's resolved height would swing with the CURRENT
    // scroll offset every frame -- exactly the "parent container expands on initial scroll, then
    // fully wraps when scrolled down" live report this depth counter exists to fix. Wrap a
    // scrollable viewport's own content dispatch with [withMeasuredSubtreeIsolated] so its
    // children's claims never reach any tracker (max-right/bottom, excluding-fill variants, or
    // the measuredSlots/measuredWeights lists) -- only the scroll viewport's own already-resolved
    // slot (recorded separately, before this block runs) should ever count toward an ancestor's
    // WrapContent size.
    private var wrapContributionSuppressionDepth = 0

    internal fun recordMeasuredSlot(
        slot: UiBounds,
        contributesToWrapWidth: Boolean = true,
        contributesToWrapHeight: Boolean = true,
    ) {
        if (measuring && wrapContributionSuppressionDepth == 0) {
            measurement.record(
                slot,
                contributesToWrapWidth,
                contributesToWrapHeight,
                contributesToChildList = recordingSuppressionDepth == 0,
            )
        }
    }

    internal fun recordMeasuredWeight(weight: LayoutWeight?) {
        if (measuring) {
            if (wrapContributionSuppressionDepth == 0) {
                measurement.recordWeight(weight, contributesToChildList = recordingSuppressionDepth == 0)
            }
        } else if (weight != null && weightObservations.isNotEmpty()) {
            // Real pass: the same claim the trial would have counted, observed live. No depth
            // gate here -- the observation stack is already per-node, and both suppression
            // brackets push their own frame (see [withMeasuredRecordingSuppressed]/
            // [withMeasuredSubtreeIsolated]), so a claim lands on exactly the node the trial
            // would have credited it to.
            val top = weightObservations.lastIndex
            weightObservations[top] = weightObservations[top] or OBSERVED_WEIGHT
        }
    }

    /** Records whether a just-claimed slot filled the row/column's own main axis -- see
     * [UiMeasuredContent.fillsMainAxis]. */
    internal fun recordMeasuredMainAxisFill(fillsMainAxis: Boolean) {
        if (measuring && wrapContributionSuppressionDepth == 0) {
            measurement.recordMainAxisFill(fillsMainAxis, contributesToChildList = recordingSuppressionDepth == 0)
        }
    }

    /** See [wrapContributionSuppressionDepth]'s doc comment -- fully isolates [block]'s slot/
     * weight/main-axis-fill claims from every ancestor WrapContent tracker (not just the
     * measuredSlots/measuredWeights list [withMeasuredRecordingSuppressed] isolates). Use this
     * for a scroll viewport's own content dispatch, not ordinary composite widgets -- those
     * still want their descendants to dictate an ancestor's WrapContent hugging. */
    internal inline fun <T> withMeasuredSubtreeIsolated(block: () -> T): T {
        wrapContributionSuppressionDepth++
        pushWeightObservation()
        try {
            return block()
        } finally {
            wrapContributionSuppressionDepth--
            popWeightObservation()
        }
    }

    /** Suppresses [recordMeasuredSlot]/[recordMeasuredWeight]'s contribution to measuredSlots/
     * measuredWeights (only) for the duration of [block] -- wrap a composite child's own
     * `scope.content(slot)` call with this so its descendants' claims don't corrupt an ancestor
     * row/column's own in-progress weight-distribution accounting. Nests correctly (a depth
     * counter, not a flag) for arbitrarily deep composite children. */
    internal inline fun <T> withMeasuredRecordingSuppressed(block: () -> T): T {
        recordingSuppressionDepth++
        pushWeightObservation()
        try {
            return block()
        } finally {
            recordingSuppressionDepth--
            popWeightObservation()
        }
    }

    // --- Live weight observation + cross-frame answer reuse ---------------------------------
    // "Does this row/column have a weighted direct child" is STRUCTURAL, and the real content
    // pass at the end of row()/column() already executes the subtree -- so observe it there and
    // reuse last frame's answer to pick this frame's branch, instead of executing the whole
    // subtree a second time as a throwaway trial just to learn one boolean. See
    // docs/tasks/2026-08-02-trial-measure-double-execution.md.
    //
    // The observation stack is bracketed by [withMeasuredRecordingSuppressed], i.e. exactly the
    // same bracket that decides what counts as a DIRECT child for the trial's own
    // measuredWeights list -- so the observed answer and the trial's answer are the same answer
    // by construction, not by two rules that can drift apart.
    private val weightObservations = ArrayList<Int>()

    /** Set from the frame [withMeasuredRecordingSuppressed] just popped -- read it immediately
     * after that call returns, like row()/column() do. */
    internal var lastChildWeightObservation: Boolean = false
        private set

    internal fun pushWeightObservation() {
        weightObservations.add(0)
    }

    internal fun popWeightObservation() {
        val frame = weightObservations.removeAt(weightObservations.lastIndex)
        lastChildWeightObservation = (frame and OBSERVED_WEIGHT) != 0
    }

    /** Marks the in-progress child dispatch as one that chose the unweighted fast path from a
     * remembered answer rather than a fresh trial. A weighted child showing up anyway means the
     * structure changed since last frame: lay it out unweighted for this one frame (the observed
     * answer corrects the branch on the next) instead of the hard error that a parent which can
     * never plan weighted slots still deserves. */
    internal fun tolerateUnplannedWeightInternal() {
        if (weightObservations.isEmpty()) return
        val top = weightObservations.lastIndex
        weightObservations[top] = weightObservations[top] or TOLERATE_UNPLANNED_WEIGHT
    }

    internal fun toleratesUnplannedWeightInternal(): Boolean =
        weightObservations.isNotEmpty() &&
            (weightObservations[weightObservations.lastIndex] and TOLERATE_UNPLANNED_WEIGHT) != 0

    // Positional identity, since this DSL has no call-site identity: a rolling hash of the child
    // indices from the root to this node. Stable frame to frame exactly as long as the structure
    // is -- and when the structure DOES change the key simply misses, which costs one fresh
    // trial (the correct answer), not a wrong branch.
    private val pathCounters = ArrayList<Int>()
    private val pathHashStack = ArrayList<Long>()
    private var pathHash = 1L

    // Two maps swapped per frame rather than one that grows forever: an entry no frame touches
    // (a page that navigated away) falls out after one swap.
    private var weightAnswers = HashMap<Long, Boolean>()
    private var previousWeightAnswers = HashMap<Long, Boolean>()

    private fun beginWeightAnswerFrame() {
        pathCounters.clear()
        pathHashStack.clear()
        pathHash = 1L
        weightObservations.clear()
        val recycled = previousWeightAnswers
        previousWeightAnswers = weightAnswers
        recycled.clear()
        weightAnswers = recycled
    }

    /** Enters this row/column's positional node, returning its path key. Must be paired with
     * [exitLayoutNodeInternal]. */
    internal fun enterLayoutNodeInternal(): Long {
        val depth = pathHashStack.size
        while (pathCounters.size <= depth) pathCounters.add(0)
        val index = pathCounters[depth]
        pathCounters[depth] = index + 1
        pathHashStack.add(pathHash)
        pathHash = pathHash * 1_000_003L + (index + 1)
        return pathHash
    }

    internal fun exitLayoutNodeInternal() {
        val childDepth = pathHashStack.size
        if (pathCounters.size > childDepth) pathCounters[childDepth] = 0
        pathHash = pathHashStack.removeAt(pathHashStack.lastIndex)
    }

    /** Last frame's observed answer for this node, or null on the first frame / after a
     * structural change (both of which fall back to the trial, i.e. today's behavior). */
    internal fun rememberedHasWeightedChildInternal(nodeKey: Long): Boolean? =
        if (measuring) null else previousWeightAnswers[nodeKey]

    internal fun recordHasWeightedChildInternal(nodeKey: Long, hasWeightedChild: Boolean) {
        if (!measuring) weightAnswers[nodeKey] = hasWeightedChild
    }

    internal fun measuredContentSnapshot(): UiMeasuredContent = measurement.snapshot()

    internal fun measureColumnContentInternal(
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        height: Float = UNBOUNDED_MAIN_AXIS,
        // See [wrapContentPass] -- true only for the WrapContent-sizing trial (resolveMeasuredColumn),
        // never for the hasWeightedChild-detection or plannedSlots trials.
        wrapContentPass: Boolean = false,
        content: ColumnScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = withOwnMeasurementScope {
        measurement.measureColumnContent(
            width = width,
            gap = gap,
            insets = insets,
            height = height,
            sourceContext = this,
            wrapContentPass = wrapContentPass,
            content = content,
        )
    }

    /**
     * Runs [block] as its own measurement accounting scope, ignoring any suppression an ancestor
     * put in place.
     *
     * [recordMeasuredSlot]/[recordMeasuredWeight] gate on these depths, and the depths are
     * context-global while a nested measure pass builds a FRESH result. So a container measuring
     * its own children inside a surface or a scroll viewport -- both of which suppress during
     * content dispatch, correctly, so descendants do not corrupt an ANCESTOR's accounting -- saw
     * every weight silently dropped and concluded it had no weighted children. The suppression is
     * about what leaks upward; it should never blind a pass to its own direct children.
     */
    private inline fun <T> withOwnMeasurementScope(block: () -> T): T {
        val previousWrap = wrapContributionSuppressionDepth
        val previousRecording = recordingSuppressionDepth
        wrapContributionSuppressionDepth = 0
        recordingSuppressionDepth = 0
        try {
            return block()
        } finally {
            wrapContributionSuppressionDepth = previousWrap
            recordingSuppressionDepth = previousRecording
        }
    }

    internal fun measureRowContentInternal(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        width: Float = UNBOUNDED_MAIN_AXIS,
        // See measureColumnContentInternal's matching param -- true only for the WrapContent-sizing
        // trial (resolveMeasuredRow), never for the hasWeightedChild-detection or plannedSlots trials.
        wrapContentPass: Boolean = false,
        content: RowScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = measurement.measureRowContent(
        height = height,
        gap = gap,
        insets = insets,
        width = width,
        sourceContext = this,
        wrapContentPass = wrapContentPass,
        content = content,
    )

    private companion object {
        const val OBSERVED_WEIGHT = 1
        const val TOLERATE_UNPLANNED_WEIGHT = 2
    }

    internal fun pointerXInternal(): Float = runtime.inputState.pointerX
    internal fun pointerYInternal(): Float = runtime.inputState.pointerY
    internal fun pointerDownInternal(): Boolean = runtime.inputState.pointerDown
}
