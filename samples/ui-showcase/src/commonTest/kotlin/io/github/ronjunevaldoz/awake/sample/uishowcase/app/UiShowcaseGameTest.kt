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
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.UiShowcaseThemePreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.inspectNonOverlappingBounds
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticContentFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticOverlaps
import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnStylePreset
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.ui
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.math.abs

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
    fun uiShowcaseContentPaneUsesConfiguredShowcaseTheme() = runTest {
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

        assertTrue(chromeColor != contentColor, "the configured showcase theme should be visually distinct from the shell theme for this regression to be meaningful")

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val rounded = renderer.lastUiPrimitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertTrue(
            rounded.any { it.matchesRegion(color = contentColor, xRange = 308f..1416f, yRange = 24f..876f) },
            "desktop content pane should use the live showcase theme"
        )
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

    @Test
    fun uiShowcaseChromeUsesLightShellTheme() = runTest {
        val shellTheme = awakeShadcnTheme(dark = false)
        val renderer = RecordingRenderer()
        val game = uiShowcase()

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val expectedSidebarColor = renderSidebarSurfaceColor(shellTheme)
        val darkSidebarColor = renderSidebarSurfaceColor(AwakeShadcnTheme)
        val sidebarSurface = renderer.lastUiPrimitives
            .filterIsInstance<UiDrawPrimitive.RoundedQuad>()
            .largestWithin(xRange = 0f..300f, minWidth = 220f, minHeight = 400f)

        assertEquals(expectedSidebarColor, sidebarSurface.color, "sidebar chrome should use the dedicated light shell theme")
        assertNotEquals(darkSidebarColor, sidebarSurface.color, "the showcase shell should stay in light mode by default")
    }

    @Test
    fun uiShowcaseShellAndContentCardsDoNotOverlap() = runTest {
        val renderer = RecordingRenderer()
        val game = uiShowcase()

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val rounded = renderer.lastUiPrimitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        val sidebarSurface = rounded.largestWithin(xRange = 0f..300f, minWidth = 220f, minHeight = 400f)
        val contentSurface = rounded.largestWithin(xRange = 300f..1440f, minWidth = 900f, minHeight = 400f)
        inspectNonOverlappingBounds(
            label = "showcase shell surfaces",
            bounds = listOf(sidebarSurface.toSlot(), contentSurface.toSlot())
        ).requireClean()

        val contentCards = rounded
            .filter {
                it.x >= contentSurface.x + 12f &&
                    it.x + it.w <= contentSurface.x + contentSurface.w - 12f &&
                    it.w > 900f &&
                    it.h in 60f..400f
            }
            .sortedBy { it.y }
            .deduplicatedCards()
        assertTrue(
            contentCards.size >= 3,
            "expected preview, usage, and notes cards in the introduction page: $contentCards"
        )
        inspectNonOverlappingBounds(
            label = "showcase content cards",
            bounds = contentCards.map { it.toSlot() }
        ).requireClean()
    }

    @Test
    fun uiShowcaseThemePreviewSemanticLayoutStaysClean() {
        val frame = UiShowcaseThemePreview.render(previewMetadataFor(UiShowcaseThemePreview))
        val semantics = frame.semantics

        inspectSemanticNodes(semantics).requireClean()
        inspectSemanticContentFit(semantics, tolerancePx = 1f).requireClean()
        inspectTextTruncation(semantics).requireClean()
        inspectSemanticOverlaps(
            label = "theme control dropdowns",
            nodes = listOf(
                requireSemanticNode(semantics, "showcase-style-preset", UiSemanticRole.Dropdown),
                requireSemanticNode(semantics, "showcase-base-color", UiSemanticRole.Dropdown),
                requireSemanticNode(semantics, "showcase-theme-mode", UiSemanticRole.Dropdown),
                requireSemanticNode(semantics, "showcase-accent", UiSemanticRole.Dropdown)
            ),
            tolerancePx = 1f
        ).requireClean()
    }
}

private fun previewMetadataFor(entry: AwakeUiPreviewEntry): AwakeUiPreviewMetadata {
    val annotation = requireNotNull(entry.javaClass.getAnnotation(AwakeUiPreview::class.java)) {
        "missing @AwakeUiPreview on ${entry.javaClass.name}"
    }
    return AwakeUiPreviewMetadata(
        id = annotation.id,
        title = annotation.title,
        group = annotation.group,
        summary = annotation.summary,
        width = annotation.width,
        height = annotation.height
    )
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

private fun UiDrawPrimitive.RoundedQuad.toSlot(): UiSlot = UiSlot(x, y, w, h)

private fun List<UiDrawPrimitive.RoundedQuad>.largestWithin(
    xRange: ClosedFloatingPointRange<Float>,
    minWidth: Float,
    minHeight: Float
): UiDrawPrimitive.RoundedQuad = requireNotNull(
    filter {
        it.x >= xRange.start &&
            it.x + it.w <= xRange.endInclusive &&
            it.w >= minWidth &&
            it.h >= minHeight
    }.maxByOrNull { it.w * it.h }
) {
    "expected a rounded quad within xRange=$xRange minWidth=$minWidth minHeight=$minHeight"
}

private fun List<UiDrawPrimitive.RoundedQuad>.deduplicatedCards(): List<UiDrawPrimitive.RoundedQuad> =
    fold(mutableListOf()) { distinct, candidate ->
        val matchesExisting = distinct.any { existing ->
            abs(existing.x - candidate.x) <= 2f &&
                abs(existing.y - candidate.y) <= 2f &&
                abs(existing.w - candidate.w) <= 4f &&
                abs(existing.h - candidate.h) <= 4f
        }
        if (!matchesExisting) {
            distinct += candidate
        }
        distinct
    }

private fun renderSidebarSurfaceColor(theme: io.github.ronjunevaldoz.awake.ui.UiTheme): Color {
    val ui = io.github.ronjunevaldoz.awake.ui.UiContext()
    ui.beginFrame(360f, 240f)
    ui.ui(x = 24f, y = 24f, width = 264f, font = BitmapFont(), theme = theme) {
        awakeShadcnSurface(
            id = "sidebar-probe",
            width = Dimension.FillMax,
            height = Dimension.Fixed(120f.dp),
            variant = AwakeShadcnSurfaceVariant.Sidebar,
            style = Style { shape(16f.dp) }
        ) {
            text("Probe")
        }
    }
    val rounded = ui.endFrame().filterIsInstance<UiDrawPrimitive.RoundedQuad>()
    return requireNotNull(rounded.maxByOrNull { it.w * it.h }) { "expected sidebar probe background" }.color
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
