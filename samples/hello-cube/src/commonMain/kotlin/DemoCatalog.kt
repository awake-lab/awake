// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.border
import io.github.ronjunevaldoz.awake.ui.clip
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * A [Game] that holds another [Game] and swaps it on demand -- the composition this whole
 * repo's `Game`-injection refactor (see docs/MVP_PLAN.md's decision log, "GenericGameApplication
 * a standalone render bootstrap") was meant to make possible: no new engine abstraction
 * needed, a "demo catalog" is just a [Game] whose [render] delegates to whichever entry the
 * user picked.
 *
 * Owns the ONE [UiContext]/[BitmapFont] used for the whole frame (its own demo-picker
 * dropdown, plus whatever the current demo stages via [CubeDemo]'s constructor-injected
 * reference to the same instances) -- [Renderer.drawUi] replaces this frame's whole staged UI
 * on every call rather than accumulating across callers, so exactly one place may call
 * `ui.beginFrame`/`renderer.drawUi` per frame. Demos append their own widgets to the shared
 * [ui] instance instead of calling `renderer.drawUi` themselves.
 *
 * All demos registered here must share the vertex format
 * `VulkanGameApplication`/`WebGpuGameApplication` were constructed with (fixed once, at the
 * platform entry point, before any [Game] exists) -- a demo needing a genuinely different
 * vertex layout would need its own `RenderPipeline`, out of scope for this catalog.
 *
 * Switching demos needs a `suspend` call ([Game.ready] loads scene JSON via
 * `SceneLoader`/`readResourceBytes`), but [render] isn't `suspend` -- [switchTo] launches its
 * own coroutine (mirroring how `GenericGameApplication.create()` already does this) and swaps
 * [current] only once the new demo's [Game.ready] actually completes, so the outgoing demo
 * keeps rendering its last frame during the switch instead of a blank/broken frame.
 *
 * Also draws a debug HUD (FPS, frame time, plus whatever the current demo reports via
 * [DebugReadout]) bottom-left every frame, and logs the same lines to console once a second --
 * chosen over interactive sliders (rotation/position/frame-time drag handles) specifically
 * because a rendered+logged text readout is verifiable by an agent through console output
 * alone, whereas a slider's state only exists visually and can't be confirmed without a
 * working screenshot pipeline.
 */
class DemoCatalog : Game {
    private val ui = UiContext()
    private val font = BitmapFont()
    private lateinit var renderer: Renderer

    // Owned here, not by CubeDemo -- see CubeDemoConfig's own doc comment for why (switchTo
    // constructs a fresh CubeDemo every switch; this is what survives that).
    private val cubeDemoConfig = CubeDemoConfig()

    private val demos: List<Pair<String, () -> Game>> = listOf(
        "CUBE" to { CubeDemo(ui, font, cubeDemoConfig) },
        // PhysicsDemo now also takes (ui, font) -- its GRID toggle needs the same shared
        // UiContext/BitmapFont instance CubeDemo already appends its own widgets to. Each
        // factory closure defers construction until actually selected.
        "PHYSICS" to { PhysicsDemo(ui, font) }
    )

    private var currentIndex = 0
    private var current: Game = demos[0].second()
    private var switching = false

    // Debug HUD: FPS/frame-time are generic (every demo has a delta), the current demo's own
    // state (camera position/rotation, etc.) comes from an optional DebugReadout instead --
    // see that interface's doc comment for why this isn't just baked into Game itself.
    private var fps = 0f
    private var frameTimeMs = 0f
    private var fpsAccumulator = 0f
    private var fpsFrameCount = 0

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        current.ready(renderer)
        verifyOffscreenReadback(renderer)
    }

    /** One-shot proof that `Renderer.createRenderTarget`/`renderToTexture`/`readPixels`
     * actually work end to end -- renders the current demo's own geometry (via the optional
     * [OffscreenPreviewSource] a demo can implement, e.g. [CubeDemo]) into a small offscreen
     * target from a second camera angle, reads it back, logs the sampled center pixel, and
     * saves it as a real viewable PNG (`saveDebugPng`) -- since a raw RGBA pixel log alone
     * doesn't prove much once real geometry (not just a flat clear color) is involved. Falls
     * back to an empty draw list (clear-color-only) if the current demo doesn't implement
     * [OffscreenPreviewSource]. */
    private suspend fun verifyOffscreenReadback(renderer: Renderer) {
        val target = renderer.createRenderTarget(128, 128)
        val camera = Camera(
            eye = Vec3(2.5f, 2f, 4f),
            center = Vec3(0f, 0f, 0f),
            fovYRadians = 1f,
            near = 0.1f,
            far = 10f,
            flipYForClipSpace = renderer.flipYForClipSpace
        )
        val drawCalls = (current as? OffscreenPreviewSource)?.sampleDrawCalls() ?: emptyList()
        renderer.renderToTexture(target, camera, drawCalls)
        val pixels = renderer.readPixels(target)
        val centerOffset = ((target.height / 2) * target.width + target.width / 2) * 4
        println(
            "OFFSCREEN READBACK: ${target.width}x${target.height} (${drawCalls.size} draw calls) " +
                "center pixel RGBA = " +
                "${pixels.data[centerOffset].toInt() and 0xFF}," +
                "${pixels.data[centerOffset + 1].toInt() and 0xFF}," +
                "${pixels.data[centerOffset + 2].toInt() and 0xFF}," +
                "${pixels.data[centerOffset + 3].toInt() and 0xFF}"
        )
        saveDebugPng(pixels.data, target.width, target.height, "offscreen-debug.png")
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        ui.beginFrame(viewportWidth, viewportHeight)
        // Two separate columns, not one shared list: the demo picker (which demo is running)
        // and the current demo's own config (camera/debug-overlay controls) are different
        // concerns that used to sit in one undifferentiated column -- confirmed via a real
        // screenshot that this read as one mixed list. Top-left for the picker (top-right
        // stays the config panel, bottom-left is drawDebugHud's own text readout -- see that
        // function's doc comment for why the picker can't also live there).
        val demoPickerPanel = ui.column(x = PANEL_MARGIN, y = 20f, width = DEMO_PICKER_WIDTH, font = font, textScale = TEXT_SCALE)
        drawDemoPicker(demoPickerPanel)
        val configPanel = ui.column(x = viewportWidth - PANEL_WIDTH - PANEL_MARGIN, y = 20f, width = PANEL_WIDTH, font = font, textScale = TEXT_SCALE)
        current.render(delta, viewportWidth, viewportHeight)
        // Appended AFTER current.render() rather than before -- purely a staging-order detail
        // (UiContext collects primitives into one list regardless of when during the frame
        // they were staged, and nothing a demo's own render() does depends on its panel
        // widgets having already run this same frame), not a behavior change.
        (current as? PanelUser)?.drawPanel(configPanel)
        drawDebugHud(delta, viewportHeight)
        renderer.drawUi(ui.endFrame(), font)
    }

    private fun drawDemoPicker(panel: ColumnScope) {
        val names = demos.map { it.first }
        panel.dropdown("demo-picker", names, currentIndex, DEMO_PICKER_WIDTH, DEMO_PICKER_HEIGHT, modifier = UiModifier().clip(UiShape.sm).border(2f.dp))
            ?.let { picked -> if (picked != currentIndex) switchTo(picked) }
    }

    /** Draws FPS/frame-time + whatever [current] reports via [DebugReadout], bottom-left
     * (out of the demo-picker/catalog UI's way up top). Also logs the same lines to
     * console once per second, via [logDebugState] -- readable without a working screenshot
     * pipeline, which is the whole point (see the conversation this was designed in). */
    private fun drawDebugHud(delta: Float, viewportHeight: Float) {
        frameTimeMs = delta * 1000f
        fpsAccumulator += delta
        fpsFrameCount++
        if (fpsAccumulator >= 1f) {
            fps = fpsFrameCount / fpsAccumulator
            fpsAccumulator = 0f
            fpsFrameCount = 0
            logDebugState()
        }
        val lines = debugLines()
        lines.forEachIndexed { index, line ->
            ui.absolute(20f, viewportHeight - (lines.size - index) * HUD_LINE_HEIGHT, font, textScale = TEXT_SCALE).text(line, color = HUD_COLOR)
        }
    }

    private fun debugLines(): List<String> = buildList {
        add("FPS: ${fps.toInt()}  FRAME: ${frameTimeMs.toInt()}MS")
        (current as? DebugReadout)?.debugLines()?.let(::addAll)
    }

    private fun logDebugState() {
        println("DEBUG HUD [${demos[currentIndex].first}]")
        debugLines().forEach { println("DEBUG HUD:   $it") }
    }

    /** Constructs the picked demo, awaits its (suspend) [Game.ready] on its own coroutine,
     * then swaps [current] and disposes the outgoing demo -- see this class's doc comment
     * for why this can't just happen synchronously inside [render]. */
    private fun switchTo(index: Int) {
        if (switching) return
        switching = true
        val outgoing = current
        val incoming = demos[index].second()
        CoroutineScope(Dispatchers.Unconfined).launch {
            incoming.ready(renderer)
            outgoing.dispose()
            current = incoming
            currentIndex = index
            switching = false
        }
    }

    override fun resize(width: Float, height: Float) = current.resize(width, height)
    override fun pause() = current.pause()
    override fun resume() = current.resume()
    override fun dispose() = current.dispose()

    // --- Desktop-only debug-control channel API (see DebugControlServer.kt, desktopMain) ---
    // Callers must only invoke these from the same thread that owns this Game instance's
    // Vulkan/scene state (see this project's .claude/AGENTS.md "Threading model" section) --
    // DebugControlServer's WebSocket handler enqueues commands instead of calling these
    // directly; Main.kt's per-frame loop is the only real caller.

    /** Reuses [switchTo]'s existing suspend/coroutine swap logic -- just exposes it publicly
     * under a name that makes clear it's the debug-channel entry point, not a duplicate. */
    fun debugSwitchDemo(index: Int) = switchTo(index)

    fun debugSetCameraEye(eye: Vec3) {
        (current as? DebugCameraTarget)?.setCameraEye(eye)
    }

    fun debugSetCameraCenter(center: Vec3) {
        (current as? DebugCameraTarget)?.setCameraCenter(center)
    }

    fun debugSetMinimap(enabled: Boolean) {
        (current as? DebugMinimapTarget)?.setMinimapEnabled(enabled)
    }

    /** A snapshot of the current demo's name/HUD lines/camera -- see [DebugSnapshot]'s own
     * doc comment. `cameraEye`/`cameraCenter` are `null` only if [current] doesn't implement
     * [DebugCameraTarget] (neither shipped demo hits this today; both do). */
    fun debugSnapshot(): DebugSnapshot {
        val cameraTarget = current as? DebugCameraTarget
        return DebugSnapshot(
            demoName = demos[currentIndex].first,
            debugLines = debugLines(),
            cameraEye = cameraTarget?.getCameraEye()?.toDebugVec3(),
            cameraCenter = cameraTarget?.getCameraCenter()?.toDebugVec3(),
            minimapEnabled = (current as? DebugMinimapTarget)?.isMinimapEnabled()
        )
    }

    private fun Vec3.toDebugVec3() = DebugVec3(x, y, z)

    private companion object {
        val HUD_COLOR = floatArrayOf(0.4f, 1f, 0.4f, 1f)

        // Right-side debug-panel column geometry -- replaces the old UiLayout.kt's
        // PANEL_ROW_* constants entirely; ColumnScope's own cursor now handles per-row
        // positioning, so only the column's shared x/width need to live anywhere.
        const val PANEL_WIDTH = 300f
        const val PANEL_MARGIN = 20f
        const val DEMO_PICKER_WIDTH = 200f
        const val DEMO_PICKER_HEIGHT = 40f

        // 8px BitmapFont glyphs read as illegibly tiny at this window's actual size (confirmed
        // via a real screenshot) -- a 2x whole-number scale keeps the atlas crisp while still
        // sharing one readable size across the picker, config panel, and debug HUD.
        const val TEXT_SCALE = 2f
        const val HUD_LINE_HEIGHT = 22f
    }
}

/** Plain, JSON-serializable mirror of [Vec3] -- [Vec3] itself isn't `@Serializable` (it's an
 * `awake:base` engine type, not owned by this debug-only sample feature), so this small DTO
 * stands in for it on the wire. */
@Serializable
data class DebugVec3(val x: Float, val y: Float, val z: Float)

/** Wire shape for [DemoCatalog]'s desktop-only debug-control WebSocket channel (see
 * `DebugControlServer.kt`) -- sent back after every command (including mutating ones), so a
 * client can confirm a mutation actually took effect rather than trusting an echo of its own
 * input. */
@Serializable
data class DebugSnapshot(
    val demoName: String,
    val debugLines: List<String>,
    val cameraEye: DebugVec3?,
    val cameraCenter: DebugVec3?,
    val minimapEnabled: Boolean?
)
