// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

/**
 * One value scoped to a subtree: push on the way in, pop on the way out, read the top.
 *
 * This is Awake's equivalent of a Compose `CompositionLocal`, and the concept was already here --
 * as seven hand-written stacks in [UiContextStacks], each with its own field, getter, push and pop.
 * Nine of those fourteen members were the same two lines.
 *
 * [combine] is the part that cannot be generic-by-default. A scoped value is not always "replace
 * the parent": a text style MERGES with the one it nests inside, and alpha MULTIPLIES so nested
 * layers compound. A provider that only ever replaced would silently stop text inheriting and stop
 * alpha compounding -- both of which read as a rendering bug far from the cause. So the combine
 * rule travels with the value rather than living at the call site.
 *
 * [pop] deliberately keeps the base entry: an unbalanced pop is a caller bug, but draining the
 * stack would take every later reader with it.
 */
internal class UiScopedValue<T>(
    private val base: T,
    private val combine: (parent: T, incoming: T) -> T = { _, incoming -> incoming },
) {
    private val stack = mutableListOf(base)

    val current: T get() = stack.last()

    fun push(value: T) {
        stack.add(combine(stack.last(), value))
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.size - 1)
    }

    /**
     * Collapses to a single entry holding [value].
     *
     * A reused trial context is never popped back the way a real widget's push/pop pair is, so it
     * resets instead. Doing that uniformly is the point: the hand-written version reset six of its
     * seven stacks and missed `textStyleTokenStack`, which then grew across every reuse and left
     * `currentTextStyleToken` reporting a token from a previous trial.
     */
    fun reset(value: T = base) {
        stack.clear()
        stack.add(value)
    }
}
