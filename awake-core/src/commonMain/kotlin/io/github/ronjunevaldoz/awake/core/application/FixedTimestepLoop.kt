package io.github.ronjunevaldoz.awake.core.application

/**
 * Decouples simulation from rendering: [advance] accumulates the variable, measured
 * per-frame [GameLoop] delta and drains it in fixed-size [fixedDelta] steps (calling
 * [FixedUpdate] once per step), then calls [Render] exactly once per frame regardless of
 * how many (including zero) fixed steps ran.
 *
 * This matters once simulation does velocity/force integration (physics, Phase 8) -- a
 * variable step size there makes the simulation's behavior depend on framerate (and can
 * destabilize spring/collision integrators entirely). It's a no-op simplification today:
 * [io.github.ronjunevaldoz.awake.scene.systems.TransformSystem] only recomputes matrices
 * from the current position/rotation/scale, it doesn't integrate anything, so it would be
 * correct either way. This class exists so that guarantee holds once something *does* need
 * it, without every call site having to know or care.
 *
 * Deliberately does not interpolate rendered state between the last two fixed steps (the
 * usual pairing for this pattern, using the leftover `accumulator / fixedDelta` fraction to
 * blend the last two simulation states) -- there are no stored "previous" component values
 * to blend from yet. [render]'s `alpha` parameter is provided for a future render system
 * that wants it; today's [io.github.ronjunevaldoz.awake.scene.systems.RenderSystem] ignores
 * it and just draws whatever [FixedUpdate] last wrote.
 */
class FixedTimestepLoop(
    private val fixedDelta: Float = DEFAULT_FIXED_DELTA,
    private val maxStepsPerFrame: Int = DEFAULT_MAX_STEPS_PER_FRAME
) {
    private var accumulator = 0f

    /**
     * @param frameDelta the real, measured elapsed time since the last call (from
     *   [GameLoop.startLoop]'s `deltaTime`, cast to `Float`).
     * @param fixedUpdate called zero or more times, always with exactly [fixedDelta].
     * @param render called exactly once, with `alpha` in `[0, 1)`: how far the accumulator
     *   is into the *next* fixed step that hasn't run yet.
     */
    fun advance(frameDelta: Float, fixedUpdate: FixedUpdate, render: Render) {
        accumulator += frameDelta
        var steps = 0
        while (accumulator >= fixedDelta && steps < maxStepsPerFrame) {
            fixedUpdate(fixedDelta)
            accumulator -= fixedDelta
            steps += 1
        }
        if (steps == maxStepsPerFrame) {
            // Spiral-of-death guard: if the simulation can't keep up with real time (each
            // fixedUpdate call takes longer than fixedDelta itself), draining the full
            // accumulator would mean running more and more steps every subsequent frame,
            // permanently falling further behind. Drop the remainder instead -- the
            // simulation visibly slows down under sustained overload rather than freezing
            // the render thread trying to catch up.
            accumulator = 0f
        }
        render(accumulator / fixedDelta)
    }

    private companion object {
        const val DEFAULT_FIXED_DELTA = 1f / 60f
        const val DEFAULT_MAX_STEPS_PER_FRAME = 5
    }
}

typealias FixedUpdate = (fixedDelta: Float) -> Unit
typealias Render = (alpha: Float) -> Unit
