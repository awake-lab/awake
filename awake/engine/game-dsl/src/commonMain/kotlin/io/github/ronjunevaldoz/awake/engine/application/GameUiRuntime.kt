// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.AwakeUiDsl
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiBoxConstraints
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.place
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

        // Overlay calls from game setup
        spec.overlays.forEach { overlay ->
            overlay(this)
        }

        val frame = uiContext.finishFrame()
        input.textInputFocused = frame.effects.requestKeyboard

        renderer.drawUi(frame.primitives, font)
    }


    /**
     * Opens a root-level column directly from the runtime.
     *
     * Keep this distinct from nested [io.github.ronjunevaldoz.awake.ui.UiScope] `column(...)`
     * helpers so Kotlin does not silently bind nested layout calls back to the runtime
     * receiver instead of the current parent scope.
     */
    fun rootColumn(
        modifier: UiModifier = UiModifier(),
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit
    ) {
        val frame = uiContext.frameBounds()
        val requestedWidth = modifier.width ?: Dimension.FillMax
        val requestedHeight = modifier.height ?: Dimension.FillMax
        val width = when (requestedWidth) {
            is Dimension.Fixed -> requestedWidth.dp.toPx()
            Dimension.FillMax -> frame.width
            Dimension.WrapContent -> frame.width
        }
        val height = when (requestedHeight) {
            is Dimension.Fixed -> requestedHeight.dp.toPx()
            Dimension.FillMax -> frame.height
            Dimension.WrapContent -> frame.height
        }
        uiContext.createColumn(
            slot = frame.place(
                width = width,
                height = height,
                alignment = modifier.alignment ?: UiAlignment.TopStart,
                insets = modifier.insets,
                offsetX = modifier.offsetX.toPx(),
                offsetY = modifier.offsetY.toPx()
            ),
            gap = verticalArrangement.baseSpacingPx(),
            verticalArrangement = verticalArrangement,
            testTag = modifier.testTag
        ).block()
    }

    @Deprecated(
        message = "Use rootColumn(modifier = ...) so authored runtime layout comes from UiModifier, not UiSlot geometry.",
        replaceWith = ReplaceWith("rootColumn(modifier = modifier, verticalArrangement = verticalArrangement, block = block)")
    )
    fun rootColumn(
        slot: UiSlot,
        modifier: UiModifier = UiModifier(),
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit
    ) {
        uiContext.createColumn(
            slot = slot,
            gap = verticalArrangement.baseSpacingPx(),
            insets = modifier.insets,
            verticalArrangement = verticalArrangement,
            testTag = modifier.testTag
        ).block()
    }

    @Deprecated(
        message = "Use rootColumn(slot = ..., modifier = ...) or canvas { column(...) } for runtime-owned root layout.",
        level = DeprecationLevel.HIDDEN
    )
    fun column(
        modifier: UiModifier = UiModifier(),
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit
    ) = rootColumn(modifier, verticalArrangement, block)

    @Deprecated(
        message = "Use rootColumn(modifier = ...) or canvas { column(...) } for runtime-owned root layout.",
        level = DeprecationLevel.HIDDEN
    )
    fun column(
        slot: UiSlot,
        modifier: UiModifier = UiModifier(),
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit
    ) = rootColumn(slot, modifier, verticalArrangement, block)

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
