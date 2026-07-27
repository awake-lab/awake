// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.math.Camera
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
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePages
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.drawUiShowcaseSidebar
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.drawUiShowcasePageContent
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.UiShowcaseThemePreview
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.previewMetadataFor
import io.github.ronjunevaldoz.awake.testing.ui.inspectNonOverlappingBounds
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticContentFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticOverlaps
import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.createColumn
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnStylePreset
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

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

        state.showcaseStylePresetIndex = ShadcnStylePreset.Lyra.ordinal
        state.showcaseBaseColorIndex = ShadcnBaseColor.Mist.ordinal
        state.showcaseAccentIndex = ShadcnAccent.Blue.ordinal
        state.showcaseThemeModeIndex = UiShowcaseThemeMode.Light.ordinal
        state.tipsVisible = false
        state.showcaseDangerMode = true
        state.showcasePrimaryClicks = 2

        assertEquals(
            UiShowcaseUiState(
                showcaseStylePresetIndex = ShadcnStylePreset.Lyra.ordinal,
                showcaseBaseColorIndex = ShadcnBaseColor.Mist.ordinal,
                showcaseAccentIndex = ShadcnAccent.Blue.ordinal,
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
        state.showcaseStylePresetIndex = ShadcnStylePreset.Maia.ordinal
        state.showcaseBaseColorIndex = ShadcnBaseColor.Taupe.ordinal
        state.showcaseAccentIndex = ShadcnAccent.Emerald.ordinal
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
        state.showcaseStylePresetIndex = ShadcnStylePreset.Maia.ordinal
        state.showcaseBaseColorIndex = ShadcnBaseColor.Stone.ordinal
        state.showcaseAccentIndex = ShadcnAccent.Red.ordinal
        state.showcaseThemeModeIndex = UiShowcaseThemeMode.Dark.ordinal
        val spec = uiShowcaseModule(state).createGameSpec {
            title = uiShowcaseSpec().windowConfig.title
            size(1600, 900)
            backend.select(platformBackendPreference())
        }
        val game = spec.createGame()

        val chromeColor = requireNotNull(ShadcnTheme.components.surface.resolve().background)
        val contentColor =
            requireNotNull(state.showcaseTheme().components.surface.resolve().background)

        assertTrue(
            chromeColor != contentColor,
            "the configured showcase theme should be visually distinct from the shell theme for this regression to be meaningful"
        )

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val rounded = renderer.lastUiPrimitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertTrue(
            rounded.any {
                it.matchesRegion(
                    color = contentColor,
                    xRange = 308f..1416f,
                    yRange = 24f..876f
                )
            },
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
        val shellTheme = shadcnTheme(dark = false)
        val renderer = RecordingRenderer()
        val game = uiShowcase()

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val expectedSidebarColor = renderSidebarSurfaceColor(shellTheme)
        val darkSidebarColor = renderSidebarSurfaceColor(ShadcnTheme)
        val sidebarSurface = renderer.lastUiPrimitives
            .filterIsInstance<UiDrawPrimitive.RoundedQuad>()
            .largestWithin(xRange = 0f..300f, minWidth = 220f, minHeight = 400f)

        assertEquals(
            expectedSidebarColor,
            sidebarSurface.color,
            "sidebar chrome should use the dedicated light shell theme"
        )
        assertNotEquals(
            darkSidebarColor,
            sidebarSurface.color,
            "the showcase shell should stay in light mode by default"
        )
    }

    @Test
    fun uiShowcaseShellAndContentCardsDoNotOverlap() = runTest {
        val renderer = RecordingRenderer()
        val game = uiShowcase()

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val rounded = renderer.lastUiPrimitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        val sidebarSurface =
            rounded.largestWithin(xRange = 0f..300f, minWidth = 220f, minHeight = 400f)
        val contentSurface =
            rounded.largestWithin(xRange = 300f..1440f, minWidth = 900f, minHeight = 400f)
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

    @Test
    fun uiShowcaseThemingPageEnablesVerticalScrollWhenViewportIsConstrained() {
        val state = UiShowcaseRuntimeState()
        val ui = UiContext()
        val input = Input()

        ui.beginFrame(960f, 540f, input.updateSnapshot().toUiInputState())
        var selectedPage by ui.rememberStateValue("ui-showcase-page", "entry") {
            ShowcasePages.first().id
        }
        selectedPage = "theming"
        val contentScroll = ui.rememberScrollState("ui-showcase-scroll-content")

        ui.createColumn(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(720f.dp).height(516f.dp),
            font = BitmapFont(),
            theme = state.showcaseTheme()
        ).run {
            column(
                id = "ui-showcase-content-viewport",
                modifier = (Modifier.verticalScroll(contentScroll)).width(Dimension.FillMax).height(Dimension.Fixed(320f.px))) {
                shadcnSurface(
                    id = "ui-showcase-content",
                    style = Style { shape(16f.dp) }
                , modifier = Modifier.height(Dimension.WrapContent)) {
                    drawUiShowcasePageContent(state, showInlineMenu = false)
                }
            }
        }

        ui.endFrame()
        val semantics = ui.semanticNodes()
        val viewport = requireSemanticNode(semantics, "ui-showcase-content-viewport", UiSemanticRole.ScrollPanel)

        assertTrue(contentScroll.canScrollY, "the theming page should overflow a constrained viewport")
        assertTrue(
            contentScroll.contentHeight > contentScroll.viewportHeight,
            "expected contentHeight=${'$'}{contentScroll.contentHeight} to exceed viewportHeight=${'$'}{contentScroll.viewportHeight}"
        )
        assertEquals(contentScroll.viewportHeight, requireNotNull(viewport.contentBounds).height)
    }

    @Test
    fun uiShowcaseDesktopShellDoesNotDuplicateIntroductionPageContent() {
        val state = UiShowcaseRuntimeState()
        val ui = UiContext()
        val input = Input()

        ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        var selectedPage by ui.rememberStateValue("ui-showcase-page", "entry") {
            ShowcasePages.first().id
        }
        selectedPage = "introduction"
        val sidebarScroll = ui.rememberScrollState("ui-showcase-scroll-side")
        val contentScroll = ui.rememberScrollState("ui-showcase-scroll-content")

        ui.createColumn(
            modifier = Modifier.width(1440f.dp).height(900f.dp),
            font = BitmapFont(),
            theme = shadcnTheme(dark = false)
        ).run {
            row(
                horizontalArrangement = Arrangement.spacedBy(20f.dp),
                modifier = (Modifier.fillMaxSize().padding(24f.dp)).width(Dimension.FillMax).height(Dimension.Fixed(900f.px))) {
                shadcnSidebar(
                    id = "ui-showcase-sidebar",
                    style = Style { shape(16f.dp) },
                    modifier = (Modifier.verticalScroll(sidebarScroll)).width(264f.dp.toDimension()).height(Dimension.FillMax)) {
                    drawUiShowcaseSidebar(compact = false)
                }

                column(
                    id = "ui-showcase-content-viewport",
                    modifier = (Modifier.verticalScroll(contentScroll)).width(Dimension.FillMax).height(Dimension.FillMax)) {
                    shadcnSurface(
                        id = "ui-showcase-content",
                        style = Style { shape(16f.dp) }
                    , modifier = Modifier.height(Dimension.WrapContent)) {
                        drawUiShowcasePageContent(state, showInlineMenu = false)
                    }
                }
            }
        }

        ui.endFrame()
        val semantics = ui.semanticNodes()

        assertEquals(
            1,
            semantics.count { it.id == "ui-showcase-preview-tab-introduction" && it.role == UiSemanticRole.Button },
            "desktop shell should render a single introduction Preview tab"
        )
        assertEquals(
            1,
            semantics.count { it.id == "ui-showcase-code-tab-introduction" && it.role == UiSemanticRole.Button },
            "desktop shell should render a single introduction Code tab"
        )
        assertEquals(
            1,
            semantics.count { it.id == "ui-showcase-preview-code-introduction" && it.role == UiSemanticRole.Panel },
            "desktop shell should render a single introduction preview card"
        )
    }

    @Test
    fun uiShowcaseSidebarCategoryGroupsAreCollapsibleAndDefaultExpanded() {
        val ui = UiContext()
        val input = Input()

        fun renderSidebar() {
            // Large deltaSeconds lets the collapsible's height animation converge in one frame.
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState(), deltaSeconds = 5f)
            ui.createColumn(
                modifier = Modifier.width(264f.dp).height(900f.dp),
                font = BitmapFont(),
                theme = shadcnTheme(dark = false)
            ).run {
                drawUiShowcaseSidebar(compact = false)
            }
            ui.endFrame()
        }

        renderSidebar()
        var semantics = ui.semanticNodes()
        assertTrue(
            semantics.any { it.id == "ui-showcase-sidebar-category-GettingStarted.header" },
            "expected a collapsible header for the GettingStarted category"
        )
        assertTrue(
            semantics.any { it.id == "ui-showcase-page-introduction" },
            "the selected page's category (GettingStarted) should be expanded by default"
        )

        // Collapse the GettingStarted group directly through its persisted state, same as a click would.
        var expanded by ui.rememberStateValue("ui-showcase-sidebar-category", "GettingStarted") { true }
        expanded = false

        renderSidebar()
        renderSidebar()
        semantics = ui.semanticNodes()
        assertTrue(
            semantics.none { it.id == "ui-showcase-page-introduction" },
            "collapsing the GettingStarted group should hide its page buttons"
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

private fun renderSidebarSurfaceColor(theme: UiTheme): Color {
    val ui = UiContext()
    ui.beginFrame(
        360f,
        240f,
        io.github.ronjunevaldoz.awake.core.input.Input().updateSnapshot().toUiInputState()
    )
    ui.createColumn(
        modifier = Modifier.offset(24f.dp, 24f.dp).width(264f.dp).height(180f.dp),
        font = BitmapFont(),
        theme = theme
    ).run {
        shadcnSidebar(
            id = "sidebar-probe",
            style = Style { shape(16f.dp) },
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(120f.dp))) {
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

    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?): Material =
        object : Material {
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

    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) =
        Unit

    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TextureAsset(ByteArray(target.width * target.height * 4), target.width, target.height)

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
