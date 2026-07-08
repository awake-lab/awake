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

    override fun create(surface: Any?) {
        drawables =
            listOf(
                DemoTriangle(),
                DemoTexture(),
                DemoColoredTriangle(),
                TransformTriangle(),
                FontBitmapSample(),
                CubeSample()
            )
    }


    override fun update(delta: Float) {
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
        drawables.filterIsInstance<Disposable>().forEach { it.dispose() }
    }
}