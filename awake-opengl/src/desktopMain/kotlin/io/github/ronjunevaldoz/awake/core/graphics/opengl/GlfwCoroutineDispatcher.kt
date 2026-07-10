package io.github.ronjunevaldoz.awake.core.graphics.opengl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.lwjgl.glfw.GLFW
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

class GlfwCoroutineDispatcher : CoroutineDispatcher() {

    private val tasks: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val isStopped = AtomicBoolean(false)

    fun runLoop() {
        while (!isStopped.get()) {
            val runnable = tasks.take()
            if (!isStopped.get()) {
                runnable.run()
            }
            GLFW.glfwWaitEvents()
        }
    }

    fun stop() {
        isStopped.set(true)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        tasks.offer(block)
        GLFW.glfwPostEmptyEvent()
    }

    companion object {
        private var instances: GlfwCoroutineDispatcher? = null

        fun init(): GlfwCoroutineDispatcher? {
            if (instances == null) {
                instances = GlfwCoroutineDispatcher()
            }
            return instances
        }
    }
}

val Dispatchers.Glfw: GlfwCoroutineDispatcher
    get() = GlfwCoroutineDispatcher.init()!!