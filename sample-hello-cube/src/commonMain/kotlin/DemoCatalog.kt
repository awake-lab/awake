// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
 */
class DemoCatalog : Game {
    private val ui = UiContext()
    private val font = BitmapFont()
    private lateinit var renderer: Renderer

    private val demos: List<Pair<String, () -> Game>> = listOf(
        "CUBE" to { CubeDemo(ui, font) }
        // Add new demos here as they land -- e.g. "MINIMAP" to { MinimapDemo(ui, font) }
        // once render-to-texture exists, "PHYSICS" to { PhysicsDemo(ui, font) } once Jolt
        // bindings land. Each factory closure defers construction until actually selected.
    )

    private var currentIndex = 0
    private var current: Game = demos[0].second()
    private var switching = false

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        current.ready(renderer)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        ui.beginFrame(viewportWidth, viewportHeight)
        drawDemoPicker(viewportWidth)
        current.render(delta, viewportWidth, viewportHeight)
        renderer.drawUi(ui.endFrame(), font)
    }

    private fun drawDemoPicker(viewportWidth: Float) {
        val names = demos.map { it.first }
        ui.dropdown("demo-picker", viewportWidth - 180f, 20f, 160f, 32f, names, currentIndex, font)
            ?.let { picked -> if (picked != currentIndex) switchTo(picked) }
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
}
