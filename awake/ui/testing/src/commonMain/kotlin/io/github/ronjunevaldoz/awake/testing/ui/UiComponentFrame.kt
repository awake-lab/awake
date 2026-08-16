// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont
import io.github.ronjunevaldoz.awake.ui.context.LocalTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import io.github.ronjunevaldoz.awake.ui.theme.asRuntimeTheme
import io.github.ronjunevaldoz.awake.ui.toUiInputState

/**
 * Optional composition boundary around a test-rendered Headless root.
 *
 * `ui:testing` remains neutral: a design system supplies its own implementation to install
 * locals, while ordinary Headless tests keep the default pass-through provider.
 */
typealias UiTestRootProvider = UiScope.(content: UiScope.() -> Unit) -> Unit

/**
 * One rendered frame, with lookups that say what went wrong.
 *
 * [bounds] is the one worth having. Every geometry test needs a node by id, and the hand-written
 * version is `requireNotNull(frame.semantics.firstOrNull { it.id == x })` -- which, when the id is
 * wrong or the node was never emitted, fails with "Required value was null" and nothing else. That
 * message has cost real time in this repo more than once, so this one lists the ids that were
 * actually present.
 */
class UiComponentFrame(
    val semantics: List<UiSemanticNode>,
    val root: UiBounds,
    /** This frame's draw output -- what a test asserting on painted geometry (a separator's
     * 1px line, an avatar's badge circle) needs, and the reason those tests used to hand-roll
     * the whole UiContext preamble instead of using this helper. */
    val primitives: List<UiDrawPrimitive> = emptyList(),
    /** Font installed for this frame, for preview validation and text-geometry assertions. */
    val font: UiFont = UiFonts.default(),
) {
    /** Draw primitives of one kind, in emission order. */
    inline fun <reified T : UiDrawPrimitive> primitivesOf(): List<T> = primitives.filterIsInstance<T>()

    fun nodeOrNull(id: String): UiSemanticNode? = semantics.firstOrNull { it.id == id }

    fun node(id: String): UiSemanticNode = requireNotNull(nodeOrNull(id)) {
        "no semantic node '$id'. Present ids: ${semantics.mapNotNull { it.id }.sorted()}"
    }

    fun bounds(id: String): UiBounds = node(id).bounds

    fun boundsOrNull(id: String): UiBounds? = nodeOrNull(id)?.bounds

    /** Bottom edge, the value most of these assertions actually compare. */
    fun bottomOf(id: String): Float = bounds(id).let { it.y + it.height }

    fun rightOf(id: String): Float = bounds(id).let { it.x + it.width }
}

/**
 * Multi-frame sibling of [renderUiComponent] for interaction tests: one persistent context and
 * input across frames, so press/move/release sequences read as three `frame(...)` calls instead
 * of a hand-rolled UiContext + Input + density dance per test. Restores [UiDensity.scale] on
 * [close]; use through [uiTestSession] so the restore can't be forgotten.
 */
class UiTestSession(
    val width: Float = 400f,
    val height: Float = 400f,
    theme: UiThemeValues? = null,
    font: UiFont = UiFonts.default(),
    density: Float = 1f,
    fontScale: Float = 1f,
    private val rootProvider: UiTestRootProvider = { content -> content() },
) : AutoCloseable {
    private val previousDensity = UiDensity.scale
    private val previousFontScale = UiDensity.fontScale
    val ui = UiContext()
    val input = Input()

    init {
        UiDensity.scale = density
        UiDensity.fontScale = fontScale
        ui.pushLocal(LocalFont, font)
        if (theme != null) ui.pushLocal(LocalTheme, theme.asRuntimeTheme())
    }

    /** Renders one frame with the pointer at ([x], [y]) and returns its [UiComponentFrame]. */
    fun frame(
        x: Float = -100f,
        y: Float = -100f,
        down: Boolean = false,
        deltaSeconds: Float = 1f / 60f,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame {
        input.setPointer(down = down, x = x, y = y)
        return frame(input.updateSnapshot().toUiInputState(), deltaSeconds, content)
    }

    /** Renders a frame from an exact input snapshot (wheel, keyboard, or secondary pointer). */
    fun frame(
        input: UiInputState,
        deltaSeconds: Float = 1f / 60f,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame {
        ui.beginFrame(UiFrameInput(width, height, input, deltaSeconds))
        val root = UiBounds(0f, 0f, width, height)
        ui.createUiScope(root).rootProvider { content(root) }
        val frame = ui.finishFrame()
        return UiComponentFrame(
            semantics = frame.semantics,
            root = root,
            primitives = frame.primitives,
            font = ui.current(LocalFont),
        )
    }

    /** Renders one hover frame at ([x], [y]). */
    fun hover(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame =
        frame(x = x, y = y, down = false, content = content)

    /** Presses then releases the primary pointer at one position; returns the release frame. */
    fun click(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame {
        frame(x = x, y = y, down = true, content = content)
        return frame(x = x, y = y, down = false, content = content)
    }

    /** Performs two complete primary clicks; returns the second release frame. */
    fun doubleClick(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame {
        click(x, y, content)
        return click(x, y, content)
    }

    /** Holds the primary pointer for [durationSeconds], then releases it. */
    fun longPress(
        x: Float,
        y: Float,
        durationSeconds: Float,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame {
        require(durationSeconds >= 0f) { "durationSeconds must be non-negative" }
        frame(x = x, y = y, down = true, content = content)
        frame(x = x, y = y, down = true, deltaSeconds = durationSeconds, content = content)
        return frame(x = x, y = y, down = false, content = content)
    }

    /** Presses then releases the secondary pointer at one position; returns the release frame. */
    fun rightClick(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame {
        fun secondaryFrame(down: Boolean): UiComponentFrame {
            input.setPointer(down = false, x = x, y = y)
            input.setSecondaryPointer(down)
            return frame(input.updateSnapshot().toUiInputState(), content = content)
        }
        secondaryFrame(down = true)
        return secondaryFrame(down = false)
    }

    /** Drags the primary pointer from start to end over [steps] move frames, then releases. */
    fun drag(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        steps: Int = 1,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame {
        require(steps > 0) { "steps must be positive" }
        frame(x = startX, y = startY, down = true, content = content)
        repeat(steps) { index ->
            val fraction = (index + 1).toFloat() / steps
            frame(
                x = startX + (endX - startX) * fraction,
                y = startY + (endY - startY) * fraction,
                down = true,
                content = content,
            )
        }
        return frame(x = endX, y = endY, down = false, content = content)
    }

    override fun close() {
        UiDensity.scale = previousDensity
        UiDensity.fontScale = previousFontScale
    }
}

/** Scoped [UiTestSession]: density restore is guaranteed even when an assertion throws. */
fun <T> uiTestSession(
    width: Float = 400f,
    height: Float = 400f,
    theme: UiThemeValues? = null,
    font: UiFont = UiFonts.default(),
    density: Float = 1f,
    fontScale: Float = 1f,
    rootProvider: UiTestRootProvider = { content -> content() },
    block: UiTestSession.() -> T,
): T = UiTestSession(width, height, theme, font, density, fontScale, rootProvider).use(block)

/**
 * Renders [content] in a frame and returns it, replacing the six lines every component test opens
 * with: construct a context, push a font, push a theme, begin a frame with a pointer parked
 * off-screen, render, finish.
 *
 * [density] is a parameter because previews render at scale 2 while unit tests default to 1, and
 * that difference has hidden a real bug -- a suite of sidebar tests passed at scale 1 while the
 * scale-2 preview rendered the footer on top of the menu. Setting it here also restores the
 * previous value afterwards, which the hand-rolled versions had to remember to do in a `finally`.
 *
 * Deliberately not a receiver-scoped DSL: the content lambda takes the real [UiScope] so a test
 * calls widgets exactly the way production code does. A wrapper scope would be a second way to
 * build UI that only tests use, and it would drift.
 */
fun renderUiComponent(
    width: Float = 400f,
    height: Float = 400f,
    // UiThemeValues, not the runtime UiTheme: every design-system caller holds the values form
    // (ShadcnTheme is one), and pushTheme has an overload that converts. Taking the runtime type
    // would make every call site do the conversion by hand.
    theme: UiThemeValues? = null,
    font: UiFont = UiFonts.default(),
    density: Float = 1f,
    fontScale: Float = 1f,
    input: UiInputState = testSnapshot(),
    deltaSeconds: Float = 1f / 60f,
    rootProvider: UiTestRootProvider = { content -> content() },
    content: UiScope.(root: UiBounds) -> Unit,
): UiComponentFrame {
    val previousDensity = UiDensity.scale
    val previousFontScale = UiDensity.fontScale
    UiDensity.scale = density
    UiDensity.fontScale = fontScale
    try {
        val ui = UiContext()
        ui.pushLocal(LocalFont, font)
        if (theme != null) ui.pushLocal(LocalTheme, theme.asRuntimeTheme())
        ui.beginFrame(UiFrameInput(width, height, input, deltaSeconds))
        val root = UiBounds(0f, 0f, width, height)
        ui.createUiScope(root).rootProvider { content(root) }
        val frame = ui.finishFrame()
        return UiComponentFrame(
            semantics = frame.semantics,
            root = root,
            primitives = frame.primitives,
            font = ui.current(LocalFont),
        )
    } finally {
        UiDensity.scale = previousDensity
        UiDensity.fontScale = previousFontScale
    }
}
