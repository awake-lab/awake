// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.application.createGameSpec
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterContract
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterStore
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseThemeMode
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseUiState
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.drawUiShowcaseTopBar
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.measureUiShowcaseTopBarHeight
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnStylePreset
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnDropdown
import io.github.ronjunevaldoz.awake.testing.ui.inspectBoundsFit
import io.github.ronjunevaldoz.awake.testing.ui.measureUiFrame
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.measureDslColumnContent
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.ui
import io.github.ronjunevaldoz.awake.ui.verticalPx
import kotlin.math.abs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiShowcaseGameTest {

    @Test
    fun uiShowcaseBuildsReusableSpec() {
        val spec = uiShowcaseSpec()
        val game = spec.createGame()

        assertEquals("Awake UI Showcase", spec.windowConfig.title)
        assertEquals(1600, game.windowConfig.width)
        assertEquals(900, game.windowConfig.height)
        assertTrue(
            game.windowConfig.backend == GameWindowBackend.VULKAN ||
                game.windowConfig.backend == GameWindowBackend.WEBGPU
        )
    }

    @Test
    fun uiShowcaseRendersCatalogUi() = runTest {
        val renderer = RecordingRenderer()
        val game = uiShowcase()

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun uiShowcaseStateContainerPublishesUiStateFlow() {
        val state = UiShowcaseRuntimeState()

        state.showcaseStylePresetIndex = AwakeShadcnStylePreset.Lyra.ordinal
        state.showcaseBaseColorIndex = AwakeShadcnBaseColor.Mist.ordinal
        state.showcaseAccentIndex = AwakeShadcnAccent.Blue.ordinal
        state.showcaseThemeModeIndex = UiShowcaseThemeMode.Light.ordinal
        state.tipsVisible = false
        state.showcaseDangerMode = true
        state.showcasePrimaryClicks = 2

        assertEquals(
            UiShowcaseUiState(
                showcaseStylePresetIndex = AwakeShadcnStylePreset.Lyra.ordinal,
                showcaseBaseColorIndex = AwakeShadcnBaseColor.Mist.ordinal,
                showcaseAccentIndex = AwakeShadcnAccent.Blue.ordinal,
                showcaseThemeModeIndex = UiShowcaseThemeMode.Light.ordinal,
                tipsVisible = false,
                showcaseBadgeVariantIndex = 0,
                showcaseLiveBadge = true,
                showcaseDangerMode = true,
                showcaseSurfaceRadius = 12f,
                showcasePrimaryClicks = 2,
                showcaseCounterEffectMessage = null
            ),
            state.uiState.value
        )
    }

    @Test
    fun uiShowcaseDefaultsToLightThemeModeWithAutoStillAvailable() {
        val state = UiShowcaseRuntimeState()

        assertEquals(UiShowcaseThemeMode.Light, state.showcaseThemeMode())
        assertTrue(UiShowcaseThemeMode.entries.contains(UiShowcaseThemeMode.Auto))
    }

    @Test
    fun uiShowcaseStateBuildsConfiguredTheme() {
        val state = UiShowcaseRuntimeState()
        state.showcaseStylePresetIndex = AwakeShadcnStylePreset.Maia.ordinal
        state.showcaseBaseColorIndex = AwakeShadcnBaseColor.Taupe.ordinal
        state.showcaseAccentIndex = AwakeShadcnAccent.Emerald.ordinal
        state.showcaseThemeModeIndex = UiShowcaseThemeMode.Light.ordinal

        val theme = state.showcaseTheme()

        assertTrue(theme.tokens.primary != theme.tokens.secondary)
        assertTrue(theme.tokens.background != state.showcaseTheme().tokens.primary)
        assertTrue(theme.tokens.border != state.showcaseTheme().tokens.primary)
    }

    @Test
    fun uiShowcaseKeepsShellOnChromeThemeWhileContentUsesConfiguredShowcaseTheme() = runTest {
        val renderer = RecordingRenderer()
        val state = UiShowcaseRuntimeState()
        state.showcaseStylePresetIndex = AwakeShadcnStylePreset.Maia.ordinal
        state.showcaseBaseColorIndex = AwakeShadcnBaseColor.Stone.ordinal
        state.showcaseAccentIndex = AwakeShadcnAccent.Red.ordinal
        state.showcaseThemeModeIndex = UiShowcaseThemeMode.Dark.ordinal
        val spec = uiShowcaseModule(state).createGameSpec {
            title = uiShowcaseSpec().windowConfig.title
            size(1600, 900)
            backend.select(platformBackendPreference())
        }
        val game = spec.createGame()

        val chromeColor = requireNotNull(AwakeShadcnTheme.components.panel.resolve().background)
        val contentColor = requireNotNull(state.showcaseTheme().components.panel.resolve().background)
        val topBarHeight = measureUiShowcaseTopBarHeight(
            context = UiContext(),
            font = UiFonts.default(),
            state = state,
            compact = false,
            width = 1392f
        )
        val bodyTop = 24f + topBarHeight + 16f

        assertTrue(chromeColor != contentColor, "the configured showcase theme should be visually distinct from the shell theme for this regression to be meaningful")

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val rounded = renderer.lastUiPrimitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertTrue(
            rounded.any { it.matchesRegion(color = chromeColor, xRange = 24f..1416f, yRange = 24f..(24f + topBarHeight)) },
            "desktop top bar should keep the default chrome theme"
        )
        assertTrue(
            rounded.any { it.matchesRegion(color = contentColor, xRange = 308f..1416f, yRange = bodyTop..876f) },
            "desktop content pane should use the live showcase theme"
        )
    }

    @Test
    fun uiShowcaseDesktopTopBarFitsInsideItsChromeSurface() {
        val state = UiShowcaseRuntimeState()
        val context = UiContext()
        val font = UiFonts.default()
        val frame = UiSlot(
            0f,
            0f,
            1392f,
            measureUiShowcaseTopBarHeight(
                context = context,
                font = font,
                state = state,
                compact = false,
                width = 1392f
            )
        )

        context.beginFrame(frame.width, frame.height)
        context.ui(
            x = 0f,
            y = 0f,
            width = frame.width,
            font = font,
            theme = AwakeShadcnTheme,
            gap = 12f
        ) {
            awakeShadcnSurface(
                id = "ui-showcase-topbar-test",
                height = Dimension.Fixed(frame.height.dp)
            ) {
                drawUiShowcaseTopBar(state = state, compact = false)
            }
        }

        val metrics = measureUiFrame(context.endFrame(), frame)
        inspectBoundsFit(
            label = "desktop showcase top bar",
            metrics = metrics,
            allowedBounds = frame,
            tolerancePx = 1f
        ).requireClean()
    }

    @Test
    fun uiShowcaseDesktopTopBarControlCardFitsItsBounds() {
        val state = UiShowcaseRuntimeState()
        val frame = UiSlot(0f, 0f, 128f, 84f)
        val context = UiContext()
        val font = UiFonts.default()

        context.beginFrame(frame.width, frame.height)
        context.ui(
            x = 0f,
            y = 0f,
            width = frame.width,
            font = font,
            theme = AwakeShadcnTheme,
            gap = 6f
        ) {
            row(height = frame.height, width = Dimension.Fixed(frame.width.dp), gap = 0f) {
                panel(
                    id = "ui-showcase-topbar-style-card-test",
                    width = Dimension.Fixed(128f.dp),
                    gap = 6f,
                    style = theme.components.panel then Style {
                        shape(10f.dp)
                        contentPadding(10f.dp)
                    }
                ) {
                    text(
                        label = "Preset",
                        style = Style {
                            foreground(theme.tokens.mutedForeground)
                            textSize(theme.typography.caption)
                        }
                    )
                    awakeShadcnDropdown(
                        id = "ui-showcase-topbar-style-test",
                        options = listOf("Vega", "Nova", "Maia"),
                        selectedIndex = state.showcaseStylePresetIndex,
                        width = 104f
                    )?.let { state.showcaseStylePresetIndex = it }
                }
            }
        }

        val metrics = measureUiFrame(context.endFrame(), frame)
        inspectBoundsFit(
            label = "desktop showcase top bar control card",
            metrics = metrics,
            allowedBounds = frame,
            tolerancePx = 1f
        ).requireClean()
    }

    /**
     * Regression coverage for the topbar dead-space bug: [measureUiShowcaseTopBarHeight] is
     * called with a dp-space width (`UiBoxConstraints.maxWidthDp`) and its dp-space result
     * gets wrapped back into pixels via `Dimension.Fixed(topBarHeight.dp).toPx()`. This test
     * drives that exact real-caller shape at a non-1x [UiDensity.scale] (e.g. a retina
     * window) and asserts the resulting fixed pixel height of the topbar surface actually
     * matches what the content needs when measured directly at the true pixel width -- a
     * unit mismatch here silently doubled/inflated the surface height, which the existing
     * bounds-fit tests below could never catch since their `allowedBounds` is derived from
     * the very same (possibly wrong) measurement they're checking.
     */
    @Test
    fun uiShowcaseTopBarFixedHeightMatchesActualContentAtRetinaDensity() {
        val state = UiShowcaseRuntimeState()
        val font = UiFonts.default()
        val originalScale = UiDensity.scale
        try {
            UiDensity.scale = 2f
            val outerPadding = 24f
            val topBarWidthDp = 1392f - (outerPadding * 2f)

            val topBarHeightDp = measureUiShowcaseTopBarHeight(
                context = UiContext(),
                font = font,
                state = state,
                compact = false,
                width = topBarWidthDp
            )
            val fixedSurfacePx = topBarHeightDp.dp.toPx()

            // Ground truth: measure the same content directly at the true pixel width the
            // surface actually receives once claimSlot resolves Dimension.Fixed(...dp).
            val truePxWidth = topBarWidthDp.dp.toPx()
            val groundTruthContext = UiContext()
            val resolved = groundTruthContext.absolute(0f, 0f, font = font, theme = AwakeShadcnTheme)
                .resolveStyle(style = AwakeShadcnTheme.components.panel then Style { shape(16f.dp) })
            val measured = groundTruthContext.measureDslColumnContent(
                width = (truePxWidth - resolved.contentPadding.horizontalPx()).coerceAtLeast(0f),
                font = font,
                theme = AwakeShadcnTheme,
                gap = UiSpacing.sm.toPx(),
                textScale = resolved.textScale
            ) { drawUiShowcaseTopBar(state = state, compact = false) }
            val truePxHeight = measured.height + resolved.contentPadding.verticalPx()

            assertTrue(
                abs(fixedSurfacePx - truePxHeight) < 4f,
                "topbar surface pixel height ($fixedSurfacePx) drifted from its actual content " +
                    "pixel height ($truePxHeight) at UiDensity.scale=2 -- dp/px unit mismatch regression"
            )
        } finally {
            UiDensity.scale = originalScale
        }
    }

    @Test
    fun uiShowcaseCounterStoreReducesStateAndPublishesEffects() {
        val store = UiShowcaseCounterStore()

        repeat(5) {
            store.dispatch(UiShowcaseCounterContract.Intent.Increment)
        }

        assertEquals(5, store.state.value.count)
        assertEquals(
            listOf(UiShowcaseCounterContract.Effect.MilestoneReached(5)),
            store.drainEffects()
        )

        store.dispatch(UiShowcaseCounterContract.Intent.Reset)

        assertEquals(0, store.state.value.count)
        assertEquals(
            listOf(UiShowcaseCounterContract.Effect.ResetCompleted),
            store.drainEffects()
        )
    }
}

private fun UiDrawPrimitive.RoundedQuad.matchesRegion(
    color: Color,
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
    tolerance: Float = 2f
): Boolean {
    val left = x
    val top = y
    val right = x + w
    val bottom = y + h
    return this.color == color &&
        left >= xRange.start - tolerance &&
        right <= xRange.endInclusive + tolerance &&
        top >= yRange.start - tolerance &&
        bottom <= yRange.endInclusive + tolerance
}

private class RecordingRenderer : Renderer {
    var lastUiPrimitives: List<UiDrawPrimitive> = emptyList()

    override val flipYForClipSpace: Boolean = false

    override fun createMesh(geometry: MeshGeometry): Mesh = object : Mesh {
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?): Material = object : Material {
        override fun updateUniformBuffer(mvp: FloatArray) = Unit
        override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createRenderTarget(width: Int, height: Int): RenderTarget = object : RenderTarget {
        override val width: Int = width
        override val height: Int = height
        override fun destroy() = Unit
    }

    override fun draw(camera: Camera, drawCalls: List<DrawCall>) = Unit

    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) = Unit

    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TextureAsset(ByteArray(target.width * target.height * 4), target.width, target.height)

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
