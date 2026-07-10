package io.github.ronjunevaldoz.awake.core.shader

import io.github.aakira.napier.Napier

interface Shader {
    val program: Int
    fun compile()
    fun begin()
    fun end()
    fun delete()
}

fun Shader.use(content: Shader.() -> Unit) {
    when {
        program > 0 -> {
            begin()
            content()
            end()
        }

        else -> Napier.w("Invalid shader program id: $program")
    }
}