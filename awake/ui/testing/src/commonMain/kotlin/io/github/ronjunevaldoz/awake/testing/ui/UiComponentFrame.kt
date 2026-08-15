// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import io.github.ronjunevaldoz.awake.ui.toUiInputState

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
) {
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
) : AutoCloseable {
    private val previousDensity = UiDensity.scale
    val ui = UiContext()
    val input = Input()

    init {
        UiDensity.scale = density
        ui.pushFont(font)
        if (theme != null) ui.pushTheme(theme)
    }

    /** Renders one frame with the pointer at ([x], [y]) and returns its [UiComponentFrame]. */
    fun frame(
        x: Float = -100f,
        y: Float = -100f,
        down: Boolean = false,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame {
        input.setPointer(down = down, x = x, y = y)
        ui.beginFrame(width, height, input.updateSnapshot().toUiInputState())
        val root = UiBounds(0f, 0f, width, height)
        ui.createUiScope(root).content(root)
        val frame = ui.finishFrame()
        return UiComponentFrame(semantics = frame.semantics, root = root)
    }

    override fun close() {
        UiDensity.scale = previousDensity
    }
}

/** Scoped [UiTestSession]: density restore is guaranteed even when an assertion throws. */
fun <T> uiTestSession(
    width: Float = 400f,
    height: Float = 400f,
    theme: UiThemeValues? = null,
    font: UiFont = UiFonts.default(),
    density: Float = 1f,
    block: UiTestSession.() -> T,
): T = UiTestSession(width, height, theme, font, density).use(block)

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
    input: UiInputState = testSnapshot(),
    content: UiScope.(root: UiBounds) -> Unit,
): UiComponentFrame {
    val previousDensity = UiDensity.scale
    UiDensity.scale = density
    try {
        val ui = UiContext()
        ui.pushFont(font)
        if (theme != null) ui.pushTheme(theme)
        ui.beginFrame(width, height, input)
        val root = UiBounds(0f, 0f, width, height)
        ui.createUiScope(root).content(root)
        val frame = ui.finishFrame()
        return UiComponentFrame(semantics = frame.semantics, root = root)
    } finally {
        UiDensity.scale = previousDensity
    }
}
