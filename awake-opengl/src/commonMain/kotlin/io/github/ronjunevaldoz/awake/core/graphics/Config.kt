package io.github.ronjunevaldoz.awake.core.graphics

import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL

/**
 * @param fps used to limit rendering (60 frames)
 * @param ups used to limit updating (ex: 1/30)
 */
data class Config(
    var debug: Boolean = true,
    var fps: Int = 60, // frame per seconds
    var ups: Double = 1.0 / 30.0, // update per seconds
    var glVersion: OpenGL.Version = OpenGL.Version.ES3,
)