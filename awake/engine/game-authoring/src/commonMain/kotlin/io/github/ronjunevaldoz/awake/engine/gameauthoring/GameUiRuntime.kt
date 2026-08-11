// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.gameauthoring

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.game.GameInstaller
import io.github.ronjunevaldoz.awake.engine.game.GameServiceLookup
import io.github.ronjunevaldoz.awake.engine.game.GameSpecBuilder
import io.github.ronjunevaldoz.awake.engine.game.requireService
import io.github.ronjunevaldoz.awake.engine.gameauthoring.GameUiRuntime.Companion.EMPTY_UI_ONLY_CAMERA
import io.github.ronjunevaldoz.awake.engine.gameauthoring.GameUiRuntime.Companion.FRAME_TIME_HISTORY_SIZE
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.AwakeUiDsl
import io.github.ronjunevaldoz.awake.ui.UiBoxConstraints
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.debugOverlayPrimitives
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.place
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.toUiInputState

/**
 * Concrete binding between Awake's stateless [UiContext] and the stateful/suspendable
 * Game loop.
 */
@AwakeUiDsl
class GameUiRuntime(
    val services: GameServiceLookup,
    val spec: GameUiSpec,
) {
    val uiContext = UiContext()
    var theme: UiTheme = spec.theme
        private set

    var font: UiFont = spec.font

    /** Last finished frame's requested cursor (see [UiCursor]) -- `ui-core` only records the
     * request, the platform host owns the actual call. A desktop entry point wires this to
     * `runVulkanDesktopGame(cursor = { runtime.cursor })`; without that wiring nothing ever
     * applies it and every hover-driven cursor (resize handles, text fields) stays the default
     * arrow. */
    var cursor: UiCursor = UiCursor.Default
        private set

    var viewportWidth: Float = 0f
        private set
    var viewportHeight: Float = 0f
        private set

    /** Toggled by [Key.F3] (see [render], which also flips [perfStatsEnabled] to match) --
     * when on, [debugOverlayPrimitives]'s blue/green/red bounds/contentBounds/clippedBounds
     * wireframe is appended on top of every frame's real primitives. Independent of
     * [perfStatsEnabled]: a game can show frame stats without this wireframe (or vice versa)
     * by setting that flag directly, only F3 links the two as a convenience default. Off by
     * default so its per-frame work doesn't run unless requested. */
    var debugOverlayEnabled: Boolean = false

    /** Independent of [debugOverlayEnabled] -- gates [UiMeasureTrialStats] tracking and the
     * built-in [drawPerfStatsOverlay] HUD only, not the wireframe bounds overlay. A game that
     * wants its own custom stats display (reading [frameStats] directly) instead of the
     * built-in HUD can leave this false and just read [frameStats] every frame; it costs
     * nothing extra since [averageFrameTimeMs]/[fps] are always tracked regardless -- only
     * [UiMeasureTrialStats]'s row/column trial-pass counting is actually gated by this flag. */
    var perfStatsEnabled: Boolean = false

    /** Rolling window backing [averageFrameTimeMs]/[fps] -- see [recordFrameTime]. Bounded so
     * this never grows past [FRAME_TIME_HISTORY_SIZE] regardless of how long the app runs. */
    private val frameTimesMs = ArrayDeque<Float>()

    private fun recordFrameTime(deltaSeconds: Float) {
        frameTimesMs.addLast(deltaSeconds * 1000f)
        while (frameTimesMs.size > FRAME_TIME_HISTORY_SIZE) frameTimesMs.removeFirst()
    }

    val averageFrameTimeMs: Float
        get() = if (frameTimesMs.isEmpty()) 0f else frameTimesMs.sum() / frameTimesMs.size

    val fps: Float
        get() = averageFrameTimeMs.takeIf { it > 0f }?.let { 1000f / it } ?: 0f

    fun theme(theme: UiTheme) {
        this.theme = theme
    }

    /** Public (not private-set) so an installing [io.github.ronjunevaldoz.awake.engine.game.GameModule] -- see [provideDrawCalls]'s doc
     * comment -- can build its own [io.github.ronjunevaldoz.awake.render.mesh.Mesh]/
     * [io.github.ronjunevaldoz.awake.render.material.Material] via `createMesh`/`createMaterial`
     * from inside its own `onReady`/`overlay` block, the same lazy-on-demand pattern
     * [Renderer.createMesh]'s own doc comment describes. */
    lateinit var renderer: Renderer
        private set

    /** Real per-frame delta, mirrored out of [render]'s own parameter so an `overlay` block
     * (a `GameUiRuntime.() -> Unit`, no delta parameter of its own) can still advance
     * time-based state -- e.g. a spinning demo mesh's rotation angle -- without this runtime
     * inventing a separate update phase for a UI-only game. */
    var deltaSeconds: Float = 0f
        private set

    /** Optional per-frame hook an installing [io.github.ronjunevaldoz.awake.engine.game.GameModule] sets (from its own `overlay`/
     * `onReady` block, which has `this: GameUiRuntime` access) to route real 3D geometry into
     * the one [Renderer.draw] call [render] already makes every frame to drive the swapchain
     * (see [EMPTY_UI_ONLY_CAMERA]'s doc comment) -- a UI-only game has no scene/camera of its
     * own, so this is the only way a demo/game built on [GameUiRuntime] gets real mesh geometry
     * on screen. Null (the default) keeps today's UI-only behavior: [EMPTY_UI_ONLY_CAMERA] with
     * no draw calls. Whatever sets this is responsible for deciding when its own draw calls
     * should actually be included (e.g. only while its own demo/page is the active one). */
    var provideDrawCalls: (() -> Pair<Camera, List<DrawCall>>)? = null

    private companion object {
        const val FRAME_TIME_HISTORY_SIZE = 30

        /** Unused for any real 3D transform (a UI-only game has no scene/camera of its own) --
         * see [render]'s doc comment for why an otherwise-unused [Renderer.draw] call is still
         * required every frame. */
        val EMPTY_UI_ONLY_CAMERA = Camera(
            eye = Vec3(0f, 0f, 1f),
            center = Vec3(0f, 0f, 0f),
            fovYRadians = 1f,
            near = 0.1f,
            far = 10f,
        )
    }

    suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        spec.onReadyBlock(this)
    }

    fun render(
        deltaSeconds: Float,
        viewportWidth: Float,
        viewportHeight: Float,
    ) {
        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight
        this.deltaSeconds = deltaSeconds
        val input = services.requireService<Input>()
        val snapshot = input.currentSnapshot

        if (snapshot.wasPressed(Key.F3)) {
            debugOverlayEnabled = !debugOverlayEnabled
            perfStatsEnabled = debugOverlayEnabled
        }

        recordFrameTime(deltaSeconds)
        // Opt-in instrumentation (see UiMeasureTrialStats's own doc comment): only pay its
        // per-trial counting/timing cost while something that displays it is actually enabled.
        UiMeasureTrialStats.enabled = perfStatsEnabled
        if (perfStatsEnabled) UiMeasureTrialStats.reset()

        uiContext.beginFrame(
            UiFrameInput(
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                input = snapshot.toUiInputState(),
                deltaSeconds = deltaSeconds,
            ),
        )

        // Overlay calls from game setup
        spec.overlays.forEach { overlay ->
            overlay(this)
        }

        if (perfStatsEnabled) drawPerfStatsOverlay()

        val frame = uiContext.finishFrame()
        input.textInputFocused = frame.effects.requestKeyboard
        cursor = frame.effects.cursor

        val primitives = if (debugOverlayEnabled) {
            frame.primitives + uiContext.debugOverlayPrimitives()
        } else {
            frame.primitives
        }
        // A UI-only game has no `RenderSystem`/3D scene calling `Renderer.draw()` (the only
        // call that actually acquires/records/submits/presents a swapchain frame). `drawUi`
        // stages CPU-side primitives into pooled meshes first; the following `draw()` call
        // records those staged runs as an overlay pass on top of the 3D/empty frame. Without
        // this paired draw, the window shows its OS-default backing (a real desktop repro:
        // blank pale-gray window, no crash, no error, confirmed by a live GLFW/MoltenVK run).
        // `drawCalls` stays empty (falls back to `EMPTY_UI_ONLY_CAMERA`/no draw calls) unless
        // [provideDrawCalls] is set -- see its own doc comment.
        val (camera, drawCalls) = provideDrawCalls?.invoke()
            ?: (EMPTY_UI_ONLY_CAMERA to emptyList())
        renderer.drawUi(primitives, font)
        renderer.draw(camera, drawCalls)
    }

    /**
     * Opens a root-level column directly from the runtime.
     *
     * Keep this distinct from nested [io.github.ronjunevaldoz.awake.ui.UiScope] `column(...)`
     * helpers so Kotlin does not silently bind nested layout calls back to the runtime
     * receiver instead of the current parent scope.
     */
    fun rootColumn(
        modifier: UiModifier = Modifier,
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit,
    ) {
        val frame = UiBounds(0f, 0f, viewportWidth, viewportHeight)
        val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
        val requestedHeight = modifier.heightDimension ?: Dimension.FillMax
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
                offsetY = modifier.offsetY.toPx(),
            ),
            verticalArrangement = verticalArrangement,
            testTag = modifier.testTag,
        ).block()
    }

    @Deprecated(
        message = "Use rootColumn(modifier = ...) so authored runtime layout comes from UiModifier, not UiSlot geometry.",
        replaceWith = ReplaceWith("rootColumn(modifier = modifier, verticalArrangement = verticalArrangement, block = block)"),
    )
    fun rootColumn(
        slot: UiBounds,
        modifier: UiModifier = Modifier,
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit,
    ) {
        uiContext.createColumn(
            slot = slot,
            insets = modifier.insets,
            verticalArrangement = verticalArrangement,
            testTag = modifier.testTag,
        ).block()
    }

    @Deprecated(
        message = "Use rootColumn(slot = ..., modifier = ...) or frame { column(...) } for runtime-owned root layout.",
        level = DeprecationLevel.HIDDEN,
    )
    fun column(
        modifier: UiModifier = Modifier,
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit,
    ) = rootColumn(modifier, verticalArrangement, block)

    @Deprecated(
        message = "Use rootColumn(modifier = ...) or frame { column(...) } for runtime-owned root layout.",
        level = DeprecationLevel.HIDDEN,
    )
    fun column(
        slot: UiBounds,
        modifier: UiModifier = Modifier,
        verticalArrangement: Arrangement = defaultArrangement(),
        block: ColumnScope.() -> Unit,
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
    val onDisposeBlock: GameUiDisposeBlock,
) : GameInstaller {
    override fun install(into: GameSpecBuilder) {
        val runtime = GameUiRuntime(into.serviceLookup(), this)
        into.service(GameUiRuntime::class, runtime)
        into.ready { renderer -> runtime.ready(renderer) }
        into.render { delta, width, height -> runtime.render(delta, width, height) }
        into.dispose { runtime.dispose() }
    }
}

/** Root-level full-viewport box for a [GameUiRuntime] overlay -- named `frame`, not `canvas`,
 * to avoid colliding with [io.github.ronjunevaldoz.awake.ui.graphics] `CanvasScope`'s unrelated
 * raw-primitive drawing API (a real naming collision this project hit in practice). */
fun GameUiRuntime.frame(
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.(constraints: UiBoxConstraints) -> Unit,
) {
    val rootSlot = UiBounds(0f, 0f, viewportWidth, viewportHeight)
    uiContext.createBox(
        slot = rootSlot,
        contentAlignment = contentAlignment,
    ).block(
        UiBoxConstraints(
            maxWidthPx = viewportWidth,
            maxHeightPx = viewportHeight,
        ),
    )
}

/** Public Headless entry point for ordinary app/sample component trees. */
fun GameUiRuntime.headlessFrame(
    block: UiScope.() -> Unit,
) {
    val rootSlot = UiBounds(0f, 0f, viewportWidth, viewportHeight)
    uiContext.createUiScope(rootSlot).block()
}
