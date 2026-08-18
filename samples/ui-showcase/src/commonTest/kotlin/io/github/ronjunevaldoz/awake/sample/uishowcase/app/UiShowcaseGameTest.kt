// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.gameauthoring.createGameSpec
import io.github.ronjunevaldoz.awake.engine.gameauthoring.select
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterContract
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterStore
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseThemeMode
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseUiState
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePages
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.drawUiShowcasePageContent
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.drawUiShowcaseSidebar
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcasePreviewEntry
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcaseRoot
import io.github.ronjunevaldoz.awake.testing.ui.inspectNonOverlappingBounds
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticContentFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticOverlaps
import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPathCommand
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnStylePreset
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.testTag
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont

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
                game.windowConfig.backend == GameWindowBackend.WEBGPU,
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
                showcaseCounterEffectMessage = null,
            ),
            state.uiState.value,
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

        assertTrue(theme.colors.primary != theme.colors.secondary)
        assertTrue(theme.colors.background != state.showcaseTheme().colors.primary)
        assertTrue(theme.colors.border != state.showcaseTheme().colors.primary)
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

        val chromeColor = shadcnThemeValues(dark = true).colors.card
        val contentColor = state.showcaseTheme().colors.card

        assertTrue(
            chromeColor != contentColor,
            "the configured showcase theme should be visually distinct from the shell theme for this regression to be meaningful",
        )

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val rounded = renderer.lastUiPrimitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertTrue(
            rounded.any {
                it.matchesRegion(
                    color = contentColor,
                    xRange = 308f..1416f,
                    yRange = 24f..876f,
                )
            },
            "desktop content pane should use the live showcase theme",
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
            store.drainEffects(),
        )

        store.dispatch(UiShowcaseCounterContract.Intent.Reset)

        assertEquals(0, store.state.value.count)
        assertEquals(
            listOf(UiShowcaseCounterContract.Effect.ResetCompleted),
            store.drainEffects(),
        )
    }

    @Test
    fun uiShowcaseChromeUsesLightShellTheme() = runTest {
        val shellTheme = shadcnThemeValues(dark = false)
        val renderer = RecordingRenderer()
        val game = uiShowcase()

        game.ready(renderer)
        game.render(0.016f, 1440f, 900f)

        val expectedSidebarColor = renderSidebarSurfaceColor(shellTheme)
        val darkSidebarColor = renderSidebarSurfaceColor(shadcnThemeValues(dark = true))
        val sidebarSurface = renderer.lastUiPrimitives
            .filterIsInstance<UiDrawPrimitive.RoundedQuad>()
            .largestWithin(xRange = 0f..300f, minWidth = 220f, minHeight = 400f)

        assertEquals(
            expectedSidebarColor,
            sidebarSurface.color,
            "sidebar chrome should use the dedicated light shell theme",
        )
        assertNotEquals(
            darkSidebarColor,
            sidebarSurface.color,
            "the showcase shell should stay in light mode by default",
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
            rounded.largestWithin(xRange = 300f..1440f, minWidth = 800f, minHeight = 400f)
        inspectNonOverlappingBounds(
            label = "showcase shell surfaces",
            bounds = listOf(sidebarSurface.toSlot(), contentSurface.toSlot()),
        ).requireClean()

        val contentCards = rounded
            .filter {
                it.x >= contentSurface.x + 12f &&
                    it.x + it.w <= contentSurface.x + contentSurface.w - 12f &&
                    it.w >= 400f &&
                    it.h in 60f..500f
            }
            .sortedBy { it.y }
            .deduplicatedCards()
        assertTrue(
            contentCards.size >= 1,
            "expected preview/code tab content card in the introduction page: $contentCards",
        )
        inspectNonOverlappingBounds(
            label = "showcase content cards",
            bounds = contentCards.map { it.toSlot() },
        ).requireClean()
    }

    @Test
    fun uiShowcaseThemePreviewSemanticLayoutStaysClean() {
        val entry = showcasePreviewEntry("theming")
        val frame = entry.render(entry.metadata)
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
                requireSemanticNode(semantics, "showcase-accent", UiSemanticRole.Dropdown),
            ),
            tolerancePx = 1f,
        ).requireClean()
    }

    @Test
    fun uiShowcaseThemingPageEnablesVerticalScrollWhenViewportIsConstrained() {
        val state = UiShowcaseRuntimeState()
        val ui = UiContext()
        val input = Input()

        ui.beginFrame(UiFrameInput(viewportWidth = 960f, viewportHeight = 540f, input = input.updateSnapshot().toUiInputState()))
        val selectedPageState = ui.rememberStateValue("ui-showcase-page", "entry") {
            ShowcasePages.first().id
        }
        selectedPageState.value = "theming"
        val contentScroll = ui.rememberScrollState("ui-showcase-scroll-content")

        ui.pushLocal(LocalFont, BitmapFont())
        ui.showcaseRoot(theme = state.showcaseTheme(), bounds = UiBounds(24f, 24f, 720f, 516f)) {
            column(
            id = "ui-showcase-content-viewport",
            modifier = Modifier.testTag("ui-showcase-content-viewport")
                .fillMaxWidth().height(320f.dp).verticalScroll(contentScroll),
            verticalArrangement = Arrangement.Start,
            ) {
            shadcnSurface(
                id = "ui-showcase-content",
                modifier = Modifier.fillMaxWidth(),
            ) {
                drawUiShowcasePageContent(state, showInlineMenu = false)
            }
            }
        }

        ui.finishFrame().primitives
        val semantics = ui.finishFrame().semantics
        val viewport = requireSemanticNode(
            semantics,
            "ui-showcase-content-viewport",
            UiSemanticRole.ScrollPanel,
        )

        assertTrue(
            contentScroll.maxOffsetY > 0f,
            "the theming page should overflow a constrained viewport",
        )
        assertTrue(
            contentScroll.contentHeight > contentScroll.viewportHeight,
            "expected contentHeight=${'$'}{contentScroll.contentHeight} to exceed viewportHeight=${'$'}{contentScroll.viewportHeight}",
        )
        assertEquals(contentScroll.viewportHeight, requireNotNull(viewport.contentBounds).height)
    }

    @Test
    fun uiShowcaseDesktopShellDoesNotDuplicateIntroductionPageContent() {
        val state = UiShowcaseRuntimeState()
        val ui = UiContext()
        val input = Input()

        ui.beginFrame(UiFrameInput(viewportWidth = 1440f, viewportHeight = 900f, input = input.updateSnapshot().toUiInputState()))
        var selectedPage by ui.rememberStateValue("ui-showcase-page", "entry") {
            ShowcasePages.first().id
        }
        selectedPage = "introduction"
        val sidebarScroll = ui.rememberScrollState("ui-showcase-scroll-side")
        val contentScroll = ui.rememberScrollState("ui-showcase-scroll-content")

        ui.pushLocal(LocalFont, BitmapFont())
        ui.showcaseRoot(theme = shadcnThemeValues(dark = false), bounds = UiBounds(0f, 0f, 1440f, 900f)) {
            row(
            modifier = Modifier.fillMaxSize().padding(24f.dp),
            horizontalArrangement = Arrangement.spacedBy(20f.dp),
            ) {
            shadcnSidebar(
                id = "ui-showcase-sidebar",
                modifier = Modifier.verticalScroll(sidebarScroll).width(264f.dp).fillMaxHeight(),
            ) {
                drawUiShowcaseSidebar(compact = false)
            }

            column(
                modifier = Modifier.verticalScroll(contentScroll).fillMaxWidth().fillMaxHeight(),
            ) {
                shadcnSurface(
                    id = "ui-showcase-content",
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    drawUiShowcasePageContent(state, showInlineMenu = false)
                }
            }
            }
        }

        ui.finishFrame().primitives
        val semantics = ui.finishFrame().semantics

        assertEquals(
            1,
            semantics.count { it.id == "ui-showcase-preview-tab-introduction" && it.role == UiSemanticRole.Button },
            "desktop shell should render a single introduction Preview tab",
        )
        assertEquals(
            1,
            semantics.count { it.id == "ui-showcase-code-tab-introduction" && it.role == UiSemanticRole.Button },
            "desktop shell should render a single introduction Code tab",
        )
        assertEquals(
            1,
            semantics.count { it.id == "ui-showcase-preview-code-introduction" && it.role == UiSemanticRole.Panel },
            "desktop shell should render a single introduction preview card",
        )
    }

    @Test
    fun uiShowcaseSidebarCategoryGroupsAreCollapsibleAndDefaultExpanded() {
        val ui = UiContext()
        val input = Input()

        fun renderSidebar() {
            // Large deltaSeconds lets the collapsible's height animation converge in one frame.
            ui.beginFrame(UiFrameInput(viewportWidth = 1440f, viewportHeight = 900f, input = input.updateSnapshot().toUiInputState(), deltaSeconds = 5f))
            ui.pushLocal(LocalFont, BitmapFont())
            ui.showcaseRoot(theme = shadcnThemeValues(dark = false), bounds = UiBounds(0f, 0f, 264f, 900f)) {
                column { drawUiShowcaseSidebar(compact = false) }
            }
            ui.finishFrame().primitives
        }

        renderSidebar()
        var semantics = ui.finishFrame().semantics
        assertTrue(
            semantics.any { it.id == "ui-showcase-sidebar-category-GettingStarted.header" || it.id == "ui-showcase-sidebar-category-GettingStarted.trigger" },
            "expected a collapsible header for the GettingStarted category",
        )
        assertTrue(
            semantics.any { it.id == "ui-showcase-page-introduction" },
            "the selected page's category (GettingStarted) should be expanded by default",
        )

        // Collapse the GettingStarted group directly through its persisted state, same as a click would.
        var expanded by ui.rememberStateValue(
            "ui-showcase-sidebar-category",
            "GettingStarted",
        ) { true }
        expanded = false

        renderSidebar()
        renderSidebar()
        semantics = ui.finishFrame().semantics
        assertTrue(
            semantics.none { it.id == "ui-showcase-page-introduction" },
            "collapsing the GettingStarted group should hide its page buttons",
        )
    }

    @Test
    fun uiShowcaseSidebarCategoryHeaderCentersIconAndTitleVertically() {
        val ui = UiContext()
        val input = Input()

        ui.beginFrame(UiFrameInput(viewportWidth = 1440f, viewportHeight = 900f, input = input.updateSnapshot().toUiInputState(), deltaSeconds = 5f))
        ui.pushLocal(LocalFont, BitmapFont())
        ui.showcaseRoot(theme = shadcnThemeValues(dark = false), bounds = UiBounds(0f, 0f, 264f, 900f)) {
            column { drawUiShowcaseSidebar(compact = false) }
        }
        val primitives = ui.finishFrame().primitives

        val semantics = ui.finishFrame().semantics
        val header = requireSemanticNode(
            semantics,
            "ui-showcase-sidebar-category-GettingStarted.trigger",
            UiSemanticRole.Button,
        )
        val headerCenterY = header.bounds.y + header.bounds.height / 2f

        // The expand/collapse glyph is now a chevron icon (a FilledPath), not a "-"/"+" Text
        // node -- see ShadcnCollapsible.kt's trigger rebuild to match real shadcn's chevron
        // affordance. Its vertical center is read straight from the path's own command
        // coordinates instead of a semantic node's contentBounds.
        val chevron = requireNotNull(
            primitives.filterIsInstance<UiDrawPrimitive.FilledPath>().minByOrNull { primitive ->
                val ys = primitive.path.commands.mapNotNull { command ->
                    when (command) {
                        is UiPathCommand.MoveTo -> command.y
                        is UiPathCommand.LineTo -> command.y
                        else -> null
                    }
                }
                val iconY = if (ys.isNotEmpty()) (ys.min() + ys.max()) / 2f else -1000f
                abs(iconY - headerCenterY)
            },
        ) { "expected the collapsible header's chevron glyph as a FilledPath primitive" }
        val title = requireNotNull(
            semantics.firstOrNull {
                it.role == UiSemanticRole.Text &&
                    it.label == "Getting Started" &&
                    it.bounds.y >= header.bounds.y &&
                    it.bounds.y <= header.bounds.y + header.bounds.height
            },
        ) { "expected the collapsible header's category title as a Text semantic node" }

        val chevronYs = chevron.path.commands.mapNotNull { command ->
            when (command) {
                is UiPathCommand.MoveTo -> command.y
                is UiPathCommand.LineTo -> command.y
                else -> null
            }
        }
        val iconContentCenterY = (chevronYs.min() + chevronYs.max()) / 2f
        val titleContentCenterY = requireNotNull(title.contentBounds).let { it.y + it.height / 2f }

        assertTrue(
            abs(iconContentCenterY - headerCenterY) <= 15f,
            "expected the collapsible header's chevron glyph to be vertically centered in the header row: " +
                "iconCenterY=$iconContentCenterY headerCenterY=$headerCenterY",
        )
        assertTrue(
            abs(titleContentCenterY - headerCenterY) <= 15f,
            "expected the collapsible header's title to be vertically centered in the header row: " +
                "titleCenterY=$titleContentCenterY headerCenterY=$headerCenterY",
        )
    }
}

private fun UiDrawPrimitive.RoundedQuad.matchesRegion(
    color: Color,
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
    tolerance: Float = 2f,
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

private fun UiDrawPrimitive.RoundedQuad.toSlot(): UiBounds = UiBounds(x, y, w, h)

private fun List<UiDrawPrimitive.RoundedQuad>.largestWithin(
    xRange: ClosedFloatingPointRange<Float>,
    minWidth: Float,
    minHeight: Float,
): UiDrawPrimitive.RoundedQuad = requireNotNull(
    filter {
        it.x >= xRange.start &&
            it.x + it.w <= xRange.endInclusive &&
            it.w >= minWidth &&
            it.h >= minHeight
    }.maxByOrNull { it.w * it.h },
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

private fun renderSidebarSurfaceColor(theme: io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues): Color {
    val ui = UiContext()
    ui.beginFrame(UiFrameInput(viewportWidth = 360f, viewportHeight = 240f, input = Input().updateSnapshot().toUiInputState()))
    ui.pushLocal(LocalFont, BitmapFont())
    ui.showcaseRoot(theme = theme, bounds = UiBounds(24f, 24f, 264f, 180f)) {
        column {
        shadcnSidebar(
            id = "sidebar-probe",
            modifier = Modifier.fillMaxWidth().height(120f.dp),
        ) {
            text("Probe")
        }
        }
    }
    val rounded = ui.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
    return requireNotNull(rounded.maxByOrNull { it.w * it.h }) { "expected sidebar probe background" }.color
}

private class RecordingRenderer : Renderer {
    var lastUiPrimitives: List<UiDrawPrimitive> = emptyList()

    override val clipSpace: ClipSpace = ClipSpace.WebGpu
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    override var wireframe: Boolean = false
    override var shadowsEnabled: Boolean = true

    override fun createMesh(geometry: MeshGeometry): Mesh = object : Mesh {
        override val format: io.github.ronjunevaldoz.awake.render.mesh.VertexFormat =
            geometry.format

        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createMaterial(
        texture: TextureAsset?,
        renderTarget: RenderTarget?,
        uniformFloatCount: Int,
        pbrTextures: PbrTextureSet?,
    ): Material =
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

    override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) = Unit

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
