// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.WidgetState
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.scaledByAlpha
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
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

    val currentTheme: UiTheme get() = stacks.currentTheme
    val currentTextStyle: TextStyle get() = stacks.currentTextStyle
    val currentFont get() = stacks.currentFont
    val currentShapeSpec get() = stacks.currentShapeSpec
    val inputState: UiInputState get() = runtime.inputState

    fun pushTheme(theme: UiTheme) = stacks.pushTheme(theme)
    fun popTheme() = stacks.popTheme()

    fun pushTextStyle(style: TextStyle, tokenId: String? = null) = stacks.pushTextStyle(style, tokenId)
    fun popTextStyle() = stacks.popTextStyle()

    fun pushFont(font: UiFont) = stacks.pushFont(font)
    fun popFont() = stacks.popFont()

    fun pushShapeSpec(spec: io.github.ronjunevaldoz.awake.ui.UiShapeSpec?) = stacks.pushShapeSpec(spec)
    fun popShapeSpec() = stacks.popShapeSpec()

    /** See [UiContextStacks.resetForTrial] -- used only by a reused trial [UiContext]
     * (see [UiContextMeasureState.createMeasureContext]) to seed its theme/font/textStyle before
     * each trial pass without growing the stacks across reuses. */
    internal fun resetStacksForTrialInternal(theme: UiTheme, textStyle: TextStyle, font: UiFont) =
        stacks.resetForTrial(theme, textStyle, font)

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
    }

    @Deprecated(
        message = "Use beginFrame(UiFrameInput(...)) to keep the frame lifecycle input bundled as a single value.",
        replaceWith = ReplaceWith(
            "beginFrame(UiFrameInput(viewportWidth = screenWidth, viewportHeight = screenHeight, input = inputState, deltaSeconds = deltaSeconds))",
            imports = ["io.github.ronjunevaldoz.awake.ui.context.UiFrameInput"],
        ),
    )
    fun beginFrame(
        screenWidth: Float,
        screenHeight: Float,
        inputState: UiInputState,
        deltaSeconds: Float = 1f / 60f,
    ) {
        beginFrame(
            UiFrameInput(
                viewportWidth = screenWidth,
                viewportHeight = screenHeight,
                input = inputState,
                deltaSeconds = deltaSeconds,
            ),
        )
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

    fun column(
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

    fun row(
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

    @Deprecated(
        message = "Use finishFrame().ownership instead of reading intermediate input ownership directly from UiContext.",
    )
    fun inputResult(): UiInputResult = runtime.inputResult()

    @Deprecated(
        message = "Use finishFrame().primitives as the single public frame result.",
    )
    fun endFrame(): List<UiDrawPrimitive> = runtime.endFrame()

    fun finishFrame(): UiFrameOutput = runtime.finishFrame()

    internal fun onOverScrollableInternal() {
        if (!measuring) runtime.onOverScrollable()
    }

    @Deprecated(
        message = "Scrollable widget ownership should be coordinated from UiScope helpers, not public UiContext.",
    )
    fun onOverScrollable() = onOverScrollableInternal()

    internal fun onScrollConsumedInternal() {
        if (!measuring) runtime.onScrollConsumed()
    }

    @Deprecated(
        message = "Scrollable widget ownership should be coordinated from UiScope helpers, not public UiContext.",
    )
    fun onScrollConsumed() = onScrollConsumedInternal()

    @Deprecated(
        message = "Use finishFrame().semantics as the single public frame result.",
    )
    fun semanticNodes(): List<UiSemanticNode> = runtime.semanticNodes()

    internal fun hitTestInternal(slot: UiBounds): Boolean =
        !measuring && runtime.hitTest(slot)

    internal fun isActiveInternal(id: String): Boolean = runtime.isActive(id)

    internal fun tryClaimActiveInternal(id: String, hovered: Boolean) {
        if (!measuring) runtime.tryClaimActive(id, hovered)
    }

    internal fun releaseActiveIfMatchesInternal(id: String) {
        if (!measuring) runtime.releaseActiveIfMatches(id)
    }

    internal fun isFocusedInternal(id: String): Boolean = runtime.isFocused(id)

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
    internal fun pushGraphicsLayerAlphaInternal(alpha: Float) = stacks.pushAlpha(alpha)

    internal fun popGraphicsLayerAlphaInternal() = stacks.popAlpha()

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
    internal fun pushGraphicsLayerScaleInternal(transform: UiPrimitiveTransform) =
        stacks.pushTransform(transform)

    internal fun popGraphicsLayerScaleInternal() = stacks.popTransform()

    internal fun emitInternal(p: UiDrawPrimitive) {
        if (!measuring) runtime.emit(p.scaledByAlpha(stacks.currentAlpha).withTransform(stacks.currentTransform))
    }

    internal fun emitOverlayInternal(p: UiDrawPrimitive) {
        if (!measuring) runtime.emitOverlay(p.scaledByAlpha(stacks.currentAlpha).withTransform(stacks.currentTransform))
    }

    internal fun widgetStateInternal(id: String): WidgetState = runtime.widgetState(id)

    internal fun recordSemanticInternal(node: UiSemanticNode) {
        if (!measuring) runtime.recordSemantic(node)
    }

    internal fun pushClipInternal(rect: UiBounds, overlay: Boolean = false): UiBounds = runtime.pushClip(rect, overlay)

    @Deprecated(
        message = "Prefer clip helpers or UiScope-scoped clipping instead of manipulating UiContext clip stacks directly.",
    )
    fun pushClip(rect: UiBounds): UiBounds = pushClipInternal(rect)

    internal fun popClipInternal(overlay: Boolean = false): UiBounds = runtime.popClip(overlay)

    @Deprecated(
        message = "Prefer clip helpers or UiScope-scoped clipping instead of manipulating UiContext clip stacks directly.",
    )
    fun popClip(): UiBounds = popClipInternal()

    internal fun pointerDownEdgeInternal(): Boolean = runtime.pointerDownEdge()

    @Deprecated(
        message = "Pointer edge state should be read from UiScope helpers inside composition.",
    )
    fun pointerDownEdge(): Boolean = pointerDownEdgeInternal()

    internal fun setActiveInternal(id: String?) {
        runtime.setActive(id)
    }

    /** Gated by `!measuring` like [tryClaimActiveInternal]/[requestFocusInternal] -- a trial-
     * measure pass (e.g. [io.github.ronjunevaldoz.awake.ui.scope.measureColumnContent]) re-runs
     * widget code to size a WrapContent container and must not let that dry run's hover state
     * leak a cursor request into the real frame's [UiFrameOutput.effects]. */
    internal fun requestCursorInternal(cursor: UiCursor) {
        if (!measuring) runtime.requestCursor(cursor)
    }

    @Deprecated(
        message = "Active-state mutation belongs to widgets and scopes, not public UiContext callers.",
    )
    fun setActive(id: String?) {
        setActiveInternal(id)
    }

    @Deprecated(
        message = "Focus queries should go through UiScope helpers inside composition.",
    )
    fun isFocused(id: String): Boolean = isFocusedInternal(id)

    @Deprecated(
        message = "Focus mutation should go through UiScope helpers inside composition.",
    )
    fun requestFocus(id: String) = requestFocusInternal(id)

    @Deprecated(
        message = "Focus mutation should go through UiScope helpers inside composition.",
    )
    fun clearFocusIfMatches(id: String) = clearFocusIfMatchesInternal(id)

    internal fun frameDeltaSecondsInternal(): Float = runtime.frameDeltaSeconds

    @Deprecated(
        message = "Frame metrics should be read from UiScope helpers inside composition.",
    )
    fun frameDeltaSeconds(): Float = frameDeltaSecondsInternal()

    internal fun frameBoundsInternal(): UiBounds = runtime.fullFrameRect

    @Deprecated(
        message = "Frame metrics should be read from UiScope helpers inside composition.",
    )
    fun frameBounds(): UiBounds = frameBoundsInternal()

    internal fun isMeasuringInternal(): Boolean = measuring

    /** The real, persisted widget-state store this context reads/writes through
     * [rememberStateValue][io.github.ronjunevaldoz.awake.ui.rememberStateValue] et al. --
     * exposed so a trial/measurement [UiContext] (see [UiContextMeasureState]) can share it
     * with its source context instead of measuring stateful content (a `WrapContent` branch
     * that reads persisted selection/toggle state, for example) against fresh default values. */
    internal fun stateStoreInternal(): UiStateStore = stateStore

    @Deprecated(
        message = "Measurement mode is engine plumbing; prefer UiScope/layout helpers instead of branching on UiContext.",
    )
    fun isMeasuring(): Boolean = isMeasuringInternal()

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
    // UiScope.row()/UiScope.column()) -- it only excludes claims from measuredSlots/
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
        if (measuring && wrapContributionSuppressionDepth == 0) {
            measurement.recordWeight(weight, contributesToChildList = recordingSuppressionDepth == 0)
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
        try {
            return block()
        } finally {
            wrapContributionSuppressionDepth--
        }
    }

    /** Suppresses [recordMeasuredSlot]/[recordMeasuredWeight]'s contribution to measuredSlots/
     * measuredWeights (only) for the duration of [block] -- wrap a composite child's own
     * `scope.content(slot)` call with this so its descendants' claims don't corrupt an ancestor
     * row/column's own in-progress weight-distribution accounting. Nests correctly (a depth
     * counter, not a flag) for arbitrarily deep composite children. */
    internal inline fun <T> withMeasuredRecordingSuppressed(block: () -> T): T {
        recordingSuppressionDepth++
        try {
            return block()
        } finally {
            recordingSuppressionDepth--
        }
    }

    internal fun measuredContentSnapshot(): UiMeasuredContent = measurement.snapshot()

    internal fun measureColumnContentInternal(
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        height: Float = 100_000f,
        content: ColumnScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = measurement.measureColumnContent(
        width = width,
        gap = gap,
        insets = insets,
        height = height,
        sourceContext = this,
        content = content,
    )

    @Deprecated(
        message = "Measurement should be coordinated from UiScope/layout helpers, not from the public UiContext surface.",
    )
    fun measureColumnContent(
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        height: Float = 100_000f,
        content: ColumnScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = measureColumnContentInternal(
        width = width,
        gap = gap,
        insets = insets,
        height = height,
        content = content,
    )

    internal fun measureRowContentInternal(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        width: Float = 100_000f,
        content: RowScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = measurement.measureRowContent(
        height = height,
        gap = gap,
        insets = insets,
        width = width,
        sourceContext = this,
        content = content,
    )

    @Deprecated(
        message = "Measurement should be coordinated from UiScope/layout helpers, not from the public UiContext surface.",
    )
    fun measureRowContent(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        width: Float = 100_000f,
        content: RowScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = measureRowContentInternal(
        height = height,
        gap = gap,
        insets = insets,
        width = width,
        content = content,
    )

    internal fun pointerXInternal(): Float = runtime.inputState.pointerX
    internal fun pointerYInternal(): Float = runtime.inputState.pointerY
    internal fun pointerDownInternal(): Boolean = runtime.inputState.pointerDown

    @Deprecated(
        message = "Pointer coordinates should be read from UiScope helpers inside composition.",
    )
    fun pointerX(): Float = pointerXInternal()

    @Deprecated(
        message = "Pointer coordinates should be read from UiScope helpers inside composition.",
    )
    fun pointerY(): Float = pointerYInternal()

    @Deprecated(
        message = "Pointer state should be read from UiScope helpers inside composition.",
    )
    fun pointerDown(): Boolean = pointerDownInternal()
}
