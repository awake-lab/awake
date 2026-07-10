package io.github.ronjunevaldoz.awake.core.application


interface GameLoop {
    fun startLoop(onUpdate: (deltaTime: Double) -> Unit)
}