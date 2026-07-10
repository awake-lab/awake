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

package demo

import androidx.compose.ui.graphics.Color
import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.Drawable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import scene.CubeSample
import scene.DemoColoredTriangle
import scene.DemoTexture
import scene.DemoTriangle
import scene.FontBitmapSample
import scene.TransformTriangle
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object DemoApplication : Application {

    private var drawables = emptyList<Drawable>()
    val drawableLabels: List<String> = listOf(
        "Triangle",
        "Texture",
        "Colored",
        "Transform",
        "Font Bitmap",
        "Cube"
    )

    var drawableIndex: Int = 0
    var color = 0f
    var colorObject = Color.Green
    var colorVelocity = 1f / 60f
    // Web demo (see docs/MVP_PLAN.md's decision log): each scene's shader/texture loading
    // is now suspend (readResourceBytes), so create() constructs the scenes (cheap,
    // synchronous) then loads them in its own coroutine -- same "create() stays
    // synchronous, launch internally" pattern as VulkanApplication/WebGpuApplication. This
    // flag keeps update() a no-op until loading actually finishes.
    private var isReady = false

    override fun create(surface: Any?) {
        val triangle = DemoTriangle()
        val texture = DemoTexture()
        val colored = DemoColoredTriangle()
        val transform = TransformTriangle()
        val font = FontBitmapSample()
        val cube = CubeSample()
        drawables = listOf(triangle, texture, colored, transform, font, cube)
        MainScope().launch {
            triangle.load()
            texture.load()
            colored.load()
            transform.load()
            font.load()
            cube.load()
            isReady = true
        }
    }


    override fun update(delta: Float) {
        if (!isReady) return
        // Redraw background color
        if (color > 1 || color < 0) {
            colorVelocity = -colorVelocity
        }
        color += colorVelocity
        gl.clearColor(color * 0.5f, color, color, 1f)
        gl.clear(OpenGL.BufferBit.Color.value or OpenGL.BufferBit.Depth.value)

        drawables[drawableIndex].draw()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(x: Int, y: Int, width: Int, height: Int) {
        gl.viewport(x, y, width, height)
    }

    override fun dispose() {
        if (isReady) {
            drawables.filterIsInstance<Disposable>().forEach { it.dispose() }
        }
    }
}