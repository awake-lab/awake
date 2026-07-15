// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreFoundation.CFTimeInterval
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.CADisplayLink
import platform.QuartzCore.CAMetalLayer
import platform.UIKit.UIEvent
import platform.UIKit.UIScreen
import platform.UIKit.UIView
import platform.UIKit.UIWindow

/**
 * iOS Vulkan surface host -- the `CAMetalLayer`-backed [UIView] Phase 6 needs, mirroring
 * `awake-opengl`'s `GameView` (same `CADisplayLink`-driven render loop, `willMoveToWindow`
 * start/stop, `@ObjCAction` target-selector wiring), swapped from `GLKView`/OpenGL ES to a
 * bare `CAMetalLayer` added as a sublayer (deliberately not overriding `UIView.layerClass()`
 * to make the metal layer *the* backing layer -- that requires overriding an Objective-C
 * *class* method via Kotlin/Native's `UIViewMeta` companion pattern, extra ceremony this
 * sublayer approach avoids entirely for the same visual result).
 *
 * Callbacks, not `awake-core`'s `Application` interface -- `awake-vulkan` doesn't depend on
 * `awake-core` (only `awake-base`; see docs/MVP_PLAN.md's D11), and this view has no need to
 * force that dependency just to reuse one interface shape. The demo layer's own
 * `Application` implementation can adapt to these lambdas trivially.
 */
@OptIn(ExperimentalForeignApi::class)
class VulkanMetalView(
    frame: CValue<CGRect>,
    private val onCreate: (metalLayer: CAMetalLayer) -> Unit,
    private val onUpdate: (deltaSeconds: Float) -> Unit,
    private val onResize: (width: Int, height: Int) -> Unit,
    private val onPause: () -> Unit,
    private val onResume: () -> Unit
) : UIView(frame) {

    val metalLayer = CAMetalLayer()

    private var displayLink: CADisplayLink? = null
    private var previousTimestamp: CFTimeInterval = 0.0
    private var created = false

    init {
        layer.addSublayer(metalLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val scale = UIScreen.mainScreen.scale
        val width: Double
        val height: Double
        bounds.useContents {
            width = size.width * scale
            height = size.height * scale
        }
        metalLayer.frame = bounds
        metalLayer.drawableSize = CGSizeMake(width, height)
        if (!created) {
            created = true
            onCreate(metalLayer)
        } else {
            onResize(width.toInt(), height.toInt())
        }
    }

    @ObjCAction
    private fun tick(displayLink: CADisplayLink) {
        val currentTimestamp = displayLink.timestamp
        val deltaTime = (currentTimestamp - previousTimestamp).toFloat()
        previousTimestamp = currentTimestamp
        onUpdate(deltaTime)
    }

    override fun willMoveToWindow(newWindow: UIWindow?) {
        super.willMoveToWindow(newWindow)
        if (newWindow != null) {
            startRenderLoop()
        } else {
            stopRenderLoop()
        }
    }

    private fun startRenderLoop() {
        if (displayLink != null) return
        onResume()
        previousTimestamp = 0.0
        displayLink = UIScreen.mainScreen.displayLinkWithTarget(
            this,
            NSSelectorFromString("tick:")
        )?.apply {
            addToRunLoop(NSRunLoop.mainRunLoop, NSRunLoopCommonModes)
        }
    }

    private fun stopRenderLoop() {
        onPause()
        displayLink?.invalidate()
        displayLink = null
    }

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        syncAwakePointerInput(touches, down = true)
    }

    override fun touchesMoved(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesMoved(touches, withEvent)
        syncAwakePointerInput(touches, down = true)
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesEnded(touches, withEvent)
        syncAwakePointerInput(touches, down = false)
    }

    override fun touchesCancelled(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesCancelled(touches, withEvent)
        syncAwakePointerInput(touches, down = false)
    }
}
