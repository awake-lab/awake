/*
 * Awake
 * Awake.awake-demo.shared.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import demo.DemoApplication
import demo.DemoDrawer
import demo.DemoScene
import demo.VulkanScene
import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.Greeting
import io.github.ronjunevaldoz.awake.core.utils.Time
import kotlinx.coroutines.delay

@Composable
fun App() {
    MaterialTheme {
        var greetingText by remember { mutableStateOf("Hello, World!") }
        var vulkan by remember { mutableStateOf(true) }
        var fpsText by remember { mutableStateOf("") }
        // DemoApplication.drawableLabels only selects OpenGL demo scenes (DemoScene) --
        // VulkanScene always renders the same cube regardless, so showing this catalog
        // while Vulkan is enabled let you "select" items that silently did nothing. Empty
        // it out instead of showing a catalog that doesn't apply to the active renderer.
        val items = if (vulkan) emptyList() else DemoApplication.drawableLabels
        LaunchedEffect(Unit) {
            while (true) {
                delay(16)
                fpsText = "Fps: ${Time.FpsString}"
            }
        }
        // init awake context
        AwakeContext.init()
        DemoDrawer(items) {
            Button(onClick = {
                greetingText = Greeting().greet()
            }) {
                Text(greetingText)
            }
            if (!vulkan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Swipe right to open drawer samples")
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Double Arrow",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            SwitchGraphics(true) { isVulkan ->
                vulkan = isVulkan
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (vulkan) {
                    VulkanScene()
                } else {
                    DemoScene {
                        colorObject = Color.LightGray
                    }
                }
                Text(
                    text = fpsText,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SwitchGraphics(default: Boolean, onStateChange: (Boolean) -> Unit) {
    val checked = remember { mutableStateOf(default) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Enable Vulkan", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Switch(
            checked = checked.value,
            onCheckedChange = {
                checked.value = it
                onStateChange(checked.value)
            },
        )
    }
}