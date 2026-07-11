// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

// This is the real Compose UI entry point (App() -> the "Enable Vulkan" switch ->
// VulkanScene()/DemoScene()) -- what `:awake-demo:desktopApp:run` actually launches.
// It had been commented out in favor of the raw OpenGL/AWT `createFrame` demo below, which
// meant the Compose demo (and therefore Vulkan-in-Compose) was never reachable via the
// normal run task at all. The old createFrame-based entry point is preserved as
// `runOpenGlFrameDemo()` -- run manually via `./gradlew :awake-demo:desktopApp:runOpenGlFrameDemo`
// -- since it's still a legitimate standalone smoke test, just not what `run` should launch.
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Awake Demo") {
        MainView()
    }
}

fun runOpenGlFrameDemo() {
    demo.DemoApplication.let { application ->
        io.github.ronjunevaldoz.awake.core.AwakeContext.init()
        io.github.ronjunevaldoz.awake.core.application.createFrame(
            width = 640,
            height = 480,
            onInit = {
                application.create()
            },
            onResize = { width, height ->
                application.resize(0, 0, width, height)
            },
            onUpdate = { time ->
                application.update(time)
            },
            onDispose = {
                application.dispose()
            }
        )
    }
}