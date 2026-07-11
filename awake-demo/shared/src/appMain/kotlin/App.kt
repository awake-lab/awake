// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import demo.DemoDrawer
import demo.VulkanScene
import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.Greeting
import io.github.ronjunevaldoz.awake.core.utils.DebugHud
import io.github.ronjunevaldoz.awake.core.utils.Time
import kotlinx.coroutines.delay

// The legacy OpenGL demo gallery (DemoApplication/DemoScene, plus the "Enable Vulkan"
// switch that toggled between it and VulkanScene) is disabled for now: DemoApplication
// .create() eagerly constructs every sample including FontBitmapSample, whose
// NativeTrueType.createTexture() calls glGenTextures() before any GL context is current on
// this thread, crashing the JVM with "FATAL ERROR ... No context is current" on desktop
// launch. Not something this session's Vulkan/WebGPU catalog-tool work touches or needs --
// this app now always renders VulkanScene(), so the crashing OpenGL path is never
// constructed at all. Revisit (fix the OpenGL context bug, or drop the OpenGL gallery
// entirely) as a separate, dedicated slice.
@Composable
fun App() {
    MaterialTheme {
        var greetingText by remember { mutableStateOf("Hello, World!") }
        var fpsText by remember { mutableStateOf("") }
        var playerPositionText by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            while (true) {
                delay(16)
                fpsText = "Fps: ${Time.FpsString}"
                playerPositionText = DebugHud.PlayerPositionText
            }
        }
        // init awake context
        AwakeContext.init()
        DemoDrawer(emptyList()) {
            Button(onClick = {
                greetingText = Greeting().greet()
            }) {
                Text(greetingText)
            }
            Box(modifier = Modifier.fillMaxSize()) {
                VulkanScene()
                Text(
                    text = fpsText,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                )
                Text(
                    text = playerPositionText,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    color = Color.White
                )
            }
        }
    }
}