/*
 * Awake
 * Awake.awake-core.main
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

package io.github.ronjunevaldoz.awake.core.graphics.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import io.github.ronjunevaldoz.awake.core.AwakeContext

class OpenGLView(context: Context, private val renderer: AndroidRenderer) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(
            when (AwakeContext.config.glVersion) {
                OpenGL.Version.ES1 -> 1
                OpenGL.Version.ES2 -> 2
                OpenGL.Version.ES3 -> 3
            }
        )
        if (AwakeContext.config.debug) {
            debugFlags = DEBUG_CHECK_GL_ERROR
        }
        setRenderer(renderer)
    }

    override fun onPause() {
        super.onPause()
        renderer.pause()
    }

    override fun onResume() {
        super.onResume()
        renderer.resume()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderer.dispose()
    }
}