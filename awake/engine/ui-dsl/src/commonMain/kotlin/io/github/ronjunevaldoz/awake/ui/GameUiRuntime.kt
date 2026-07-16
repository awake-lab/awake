// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.GameSpecBuilder
import io.github.ronjunevaldoz.awake.engine.application.GameServiceLookup
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont

class GameUiSpec internal constructor(
    internal val theme: UiTheme,
    internal val overlays: List<GameUiOverlayBlock>,
    internal val onReadyBlock: GameUiReadyBlock,
    internal val onDisposeBlock: GameUiDisposeBlock
) : GameInstaller {
    override fun install(into: GameSpecBuilder) {
        val runtime = GameUiRuntime(
            services = into.serviceLookup(),
            spec = this
        )
        into.service(GameUiRuntime::class, runtime)
        into.ready { renderer -> runtime.ready(renderer) }
        into.render { delta, viewportWidth, viewportHeight -> runtime.render(delta, viewportWidth, viewportHeight) }
        into.dispose { runtime.dispose() }
    }
}

class GameUiRuntime internal constructor(
    private val services: GameServiceLookup,
    private val spec: GameUiSpec
) : GameServiceLookup by services {
    val uiContext = UiContext()
    val font: UiFont = BitmapFont()
    val theme: UiTheme
        get() = spec.theme

    lateinit var renderer: Renderer
        private set

    suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        spec.onReadyBlock(this)
    }

    fun render(deltaSeconds: Float, viewportWidth: Float, viewportHeight: Float) {
        if (spec.overlays.isEmpty()) {
            return
        }
        uiContext.beginFrame(viewportWidth, viewportHeight, deltaSeconds)
        spec.overlays.forEach { overlay ->
            overlay(this, viewportWidth, viewportHeight)
        }
        renderer.drawUi(uiContext.endFrame(), font)
    }

    fun dispose() {
        spec.onDisposeBlock(this)
    }

    fun column(
        x: Float,
        y: Float,
        width: Float,
        theme: UiTheme = this.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        block: UiColumnDslScope.() -> Unit
    ) {
        uiContext.ui(
            x = x,
            y = y,
            width = width,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            block = block
        )
    }

    fun column(
        slot: UiSlot,
        theme: UiTheme = this.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        block: UiColumnDslScope.() -> Unit
    ) {
        uiContext.ui(
            slot = slot,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            insets = insets,
            block = block
        )
    }

    fun absolute(
        x: Float,
        y: Float,
        theme: UiTheme = this.theme,
        textScale: Float = 1f,
        block: UiAbsoluteDslScope.() -> Unit
    ) {
        uiContext.uiAbsolute(
            x = x,
            y = y,
            font = font,
            theme = theme,
            textScale = textScale,
            block = block
        )
    }

    fun absolute(
        slot: UiSlot,
        theme: UiTheme = this.theme,
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        block: UiAbsoluteDslScope.() -> Unit
    ) {
        UiAbsoluteDslScope(
            uiContext.absolute(
                slot = slot,
                font = font,
                theme = theme,
                textScale = textScale,
                insets = insets
            )
        ).block()
    }
}
