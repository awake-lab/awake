// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.application.DesktopGameLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// GLFW key/mouse codes this sample cares about -- see glfw3.h. A small, hand-picked subset
// (matching io.github.ronjunevaldoz.awake.core.input.Key), not a full GLFW key table;
// extend as real gameplay needs more keys, rather than pre-mapping GLFW's entire key space.
private const val GLFW_KEY_SPACE = 32
private const val GLFW_KEY_ESCAPE = 256
private const val GLFW_KEY_RIGHT = 262
private const val GLFW_KEY_LEFT = 263
private const val GLFW_KEY_DOWN = 264
private const val GLFW_KEY_UP = 265
private const val GLFW_KEY_A = 65
private const val GLFW_KEY_D = 68
private const val GLFW_KEY_S = 83
private const val GLFW_KEY_W = 87
private const val GLFW_MOUSE_BUTTON_LEFT = 0
private const val GLFW_PRESS = 1

private val polledKeys = mapOf(
    GLFW_KEY_W to Key.W,
    GLFW_KEY_A to Key.A,
    GLFW_KEY_S to Key.S,
    GLFW_KEY_D to Key.D,
    GLFW_KEY_UP to Key.ArrowUp,
    GLFW_KEY_DOWN to Key.ArrowDown,
    GLFW_KEY_LEFT to Key.ArrowLeft,
    GLFW_KEY_RIGHT to Key.ArrowRight,
    GLFW_KEY_SPACE to Key.Space,
    GLFW_KEY_ESCAPE to Key.Escape
)

/**
 * Polls GLFW key/mouse state into [Input] once per frame -- called right after
 * `glfwPollEvents()`, on the same render thread (see this project's `.claude/AGENTS.md`
 * "Threading model" section), so no callback/synchronization machinery is needed.
 *
 * `glfwGetCursorPos` returns screen/logical coordinates (GLFW's own "screen coordinates",
 * NOT framebuffer pixels), but [Input.pointerX]/[Input.pointerY] must be in the same
 * FRAMEBUFFER-PIXEL space `UiContext.hitTest` and the `screenSize` uniform
 * (`swapchainManager.extent.width/height`, see `VulkanGameApplication.onRender()`) use --
 * on a Retina/HiDPI display the framebuffer is a device-pixel-ratio multiple (e.g. 2x) of
 * the logical window size, so the raw cursor position must be scaled up by
 * framebufferSize/windowSize before hit-testing, or every click lands at half its real
 * position (see the investigation this restores from git history for the original bug).
 */
private fun pollDesktopInput(window: Long) {
    polledKeys.forEach { (glfwKey, key) ->
        Input.setKeyDown(key, VulkanWindow.glfwGetKey(window, glfwKey) == GLFW_PRESS)
    }

    val cursor = VulkanWindow.glfwGetCursorPos(window)
    val windowWidth = VulkanWindow.glfwGetWindowWidth(window)
    val windowHeight = VulkanWindow.glfwGetWindowHeight(window)
    val framebufferWidth = VulkanWindow.glfwGetFramebufferWidth(window)
    val framebufferHeight = VulkanWindow.glfwGetFramebufferHeight(window)
    val scaleX = if (windowWidth != 0) framebufferWidth.toFloat() / windowWidth else 1f
    val scaleY = if (windowHeight != 0) framebufferHeight.toFloat() / windowHeight else 1f

    val leftButtonDown = VulkanWindow.glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS
    Input.setPointer(
        down = leftButtonDown,
        x = cursor[0].toFloat() * scaleX,
        y = cursor[1].toFloat() * scaleY
    )
}

/**
 * A bare GLFW window running a plain `VulkanGameApplication` injected with [DemoCatalog] --
 * same shape as `awake-demo:desktopApp`'s (now-retired) `VulkanDesktopMain.kt`, since that's
 * the minimal pattern any new consumer's own desktop entry point would follow too. Polls
 * keyboard/pointer input into [Input] once per frame (see [pollDesktopInput]) so the current
 * demo's UI widgets (e.g. the debug-toggle) are actually clickable.
 *
 * Unlike `VulkanDesktopMain.kt`, this doesn't call `AwakeContext.init()` -- that class lives
 * in `awake-opengl` (legacy backend) purely to mirror fps/ups into `EngineConfigHolder` for
 * old call sites; [DesktopGameLoop] already reads sensible defaults from
 * `EngineConfigHolder.config` without it, and a Vulkan-only sample has no reason to depend
 * on the OpenGL module at all.
 */
/** Applies one drained [DebugCommand] to [demoCatalog] -- called from `main()`'s per-frame
 * loop, on the render thread (see [DebugControlServer]'s own doc comment for why this must
 * never happen from the WebSocket handler coroutine directly). [DebugCommand.GetState] needs
 * no mutation -- the caller always reads a fresh [DemoCatalog.debugSnapshot] afterward
 * regardless of which command ran. */
private fun applyDebugCommand(demoCatalog: DemoCatalog, command: DebugCommand) {
    when (command) {
        is DebugCommand.SwitchDemo -> demoCatalog.debugSwitchDemo(command.index)
        is DebugCommand.SetCameraEye -> demoCatalog.debugSetCameraEye(Vec3(command.x, command.y, command.z))
        is DebugCommand.SetCameraCenter -> demoCatalog.debugSetCameraCenter(Vec3(command.x, command.y, command.z))
        is DebugCommand.SetMinimap -> demoCatalog.debugSetMinimap(command.enabled)
        DebugCommand.GetState -> Unit
    }
}

fun main() {
    check(VulkanWindow.glfwInit()) { "glfwInit failed" }
    VulkanWindow.glfwWindowHint(0x00022001, 0) // GLFW_CLIENT_API, GLFW_NO_API
    val window = VulkanWindow.glfwCreateWindow(800, 600, "Awake Sample - Hello Cube")
    check(window != 0L) { "glfwCreateWindow returned null" }

    val demoCatalog = DemoCatalog()
    val app = VulkanGameApplication(
        vertexShaderResourcePath = "assets/shader/vulkan/triangle.vert.spv",
        fragmentShaderResourcePath = "assets/shader/vulkan/triangle.frag.spv",
        vertexStride = sampleVertexStride,
        game = demoCatalog
    )
    app.create(window)

    // Desktop-only debug-control channel (DebugControlServer lives in :samples:server, a
    // small reusable module generic over command/response types -- see its own doc comment)
    // -- lets an AI agent drive/inspect this running demo over a WebSocket instead of
    // simulating mouse input on a real GLFW window. Started only after app.create(window)
    // so the first getState already reflects a fully-initialized demo.
    val debugServer = DebugControlServer<DebugCommand, DebugSnapshot>(
        parseCommand = ::parseDebugCommand,
        encodeResponse = { Json.encodeToString(it) }
    )
    debugServer.start()

    while (!VulkanWindow.glfwWindowShouldClose(window)) {
        VulkanWindow.glfwPollEvents()
        pollDesktopInput(window)
        // Drain+apply queued debug commands here -- same render thread as app.update(...)
        // below, per this project's "one thread owns every Vulkan call" rule.
        debugServer.drainCommands().forEach { (command, deferred) ->
            applyDebugCommand(demoCatalog, command)
            deferred.complete(demoCatalog.debugSnapshot())
        }
        DesktopGameLoop.startLoop { deltaTime ->
            app.update(deltaTime.toFloat())
        }
    }

    debugServer.stop()
    app.dispose()
}
