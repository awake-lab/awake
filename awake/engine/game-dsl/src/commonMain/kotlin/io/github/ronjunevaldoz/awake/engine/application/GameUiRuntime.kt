// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.AwakeUiDsl
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiBoxConstraints
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiScope
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
@AwakeUiDsl
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
        uiContext.bindServiceResolver { type -> services.service(type) }

        // Overlay calls from game setup
        spec.overlays.forEach { overlay ->
            overlay(this)
        }

        val result = uiContext.inputResult()
        input.textInputFocused = result.isTextInputFocused

        renderer.drawUi(uiContext.endFrame(), font)
    }


    /**
     * Opens a root-level column directly from the runtime.
     *
     * Keep this distinct from nested [io.github.ronjunevaldoz.awake.ui.UiScope] `column(...)`
     * helpers so Kotlin does not silently bind nested layout calls back to the runtime
     * receiver instead of the current parent scope.
     */
    fun rootColumn(
        x: Float,
        y: Float,
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        block: ColumnScope.() -> Unit
    ) {
        uiContext.createColumn(
            x = x,
            y = y,
            width = width,
            gap = gap,
        ).block()
    }

    /**
     * Root-level slot-based column entrypoint.
     */
    fun rootColumn(
        slot: UiSlot,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        block: ColumnScope.() -> Unit
    ) {
        uiContext.createColumn(
            slot = slot,
            gap = gap,
            insets = insets
        ).block()
    }

    @Deprecated(
        message = "Use rootColumn(...) for runtime-owned root layout. Unqualified column(...) inside nested UI scopes binds to the current UiScope instead.",
        level = DeprecationLevel.HIDDEN
    )
    fun column(
        x: Float,
        y: Float,
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        block: ColumnScope.() -> Unit
    ) = rootColumn(x, y, width, gap, block)

    @Deprecated(
        message = "Use rootColumn(...) for runtime-owned root layout. Unqualified column(slot = ...) inside nested UI scopes must bind to the current UiScope, not GameUiRuntime.",
        level = DeprecationLevel.HIDDEN
    )
    fun column(
        slot: UiSlot,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        block: ColumnScope.() -> Unit
    ) = rootColumn(slot, gap, insets, block)

    inline fun <reified T : Any> service(): T? = services.service(T::class)

    inline fun <reified T : Any> requireService(): T = services.requireService(T::class)

    fun dispose() {
        spec.onDisposeBlock(this)
    }
}

inline fun <reified T : Any> UiScope.service(): T? = context.resolveService(T::class)

inline fun <reified T : Any> UiScope.requireService(): T = checkNotNull(service<T>()) {
    "No game service registered for ${T::class.simpleName} in the current UiScope."
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
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.(constraints: UiBoxConstraints) -> Unit
) {
    val rootSlot = UiSlot(0f, 0f, viewportWidth, viewportHeight)
    uiContext.createBox(
        slot = rootSlot,
        contentAlignment = contentAlignment
    ).block(
        UiBoxConstraints(
            maxWidthPx = viewportWidth,
            maxHeightPx = viewportHeight
        )
    )
}
