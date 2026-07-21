// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiBoxConstraints
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.toUiInputState

/**
 * Concrete binding between Awake's stateless [UiContext] and the stateful/suspendable
 * Game loop.
 */
class GameUiRuntime(
    val services: GameServiceLookup,
    val spec: GameUiSpec
) {
    val uiContext = UiContext()
    var theme: UiTheme = spec.theme
        private set

    var font: UiFont = spec.font
    var viewportWidth: Float = 0f
        private set
    var viewportHeight: Float = 0f
        private set

    fun theme(theme: UiTheme) {
        this.theme = theme
    }

    private lateinit var renderer: Renderer

    suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        spec.onReadyBlock(this)
    }

    fun render(
        deltaSeconds: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ) {
        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight
        val input = services.requireService<Input>()
        val snapshot = input.currentSnapshot

        uiContext.beginFrame(
            screenWidth = viewportWidth,
            screenHeight = viewportHeight,
            inputState = snapshot.toUiInputState(),
            deltaSeconds = deltaSeconds
        )

        // Overlay calls from game setup
        spec.overlays.forEach { overlay ->
            overlay(this)
        }

        val result = uiContext.inputResult()
        input.textInputFocused = result.isTextInputFocused

        renderer.drawUi(uiContext.endFrame(), font)
    }


    fun column(
        x: Float,
        y: Float,
        width: Float,
        theme: UiTheme = this.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        block: ColumnScope.() -> Unit
    ) {
        uiContext.createColumn(
            x = x,
            y = y,
            width = width,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale
        ).block()
    }

    fun column(
        slot: UiSlot,
        theme: UiTheme = this.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        block: ColumnScope.() -> Unit
    ) {
        uiContext.createColumn(
            slot = slot,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            insets = insets
        ).block()
    }

    inline fun <reified T : Any> service(): T? = services.service(T::class)

    inline fun <reified T : Any> requireService(): T = services.requireService(T::class)

    fun dispose() {
        spec.onDisposeBlock(this)
    }
}

data class GameUiSpec(
    val theme: UiTheme,
    val font: UiFont,
    val overlays: List<GameUiOverlayBlock>,
    val onReadyBlock: GameUiReadyBlock,
    val onDisposeBlock: GameUiDisposeBlock
) : GameInstaller {
    override fun install(into: GameSpecBuilder) {
        val runtime = GameUiRuntime(into.serviceLookup(), this)
        into.service(GameUiRuntime::class, runtime)
        into.ready { renderer -> runtime.ready(renderer) }
        into.render { delta, width, height -> runtime.render(delta, width, height) }
        into.dispose { runtime.dispose() }
    }
}

fun GameUiRuntime.canvas(
    theme: UiTheme = this.theme,
    textScale: Float = 1f,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.(constraints: UiBoxConstraints) -> Unit
) {
    val rootSlot = UiSlot(0f, 0f, viewportWidth, viewportHeight)
    uiContext.createBox(
        slot = rootSlot,
        font = font,
        theme = theme,
        textScale = textScale,
        contentAlignment = contentAlignment
    ).block(
        UiBoxConstraints(
            maxWidthPx = viewportWidth,
            maxHeightPx = viewportHeight
        )
    )
}