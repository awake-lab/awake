// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

/**
 * A value that scopes to a subtree: provide it on the way in, read it anywhere below, and it is
 * restored on the way out. Awake's equivalent of a Compose `CompositionLocal`.
 *
 * Compose splits this into `compositionLocalOf` and `staticCompositionLocalOf`, and that split
 * exists purely to decide whether reading one invalidates a composition scope. This engine rebuilds
 * every frame from scratch -- there is no composition, nothing to invalidate, no subtree to
 * selectively recompose -- so both collapse into this one type. Shipping the pair would mean
 * shipping a distinction with no observable difference.
 *
 * [combine] is what a straight port would not have. A scoped value is not always "replace the
 * parent": a text style MERGES with the one it nests inside, and alpha MULTIPLIES so nested layers
 * compound. A provider that only replaced would silently stop text inheriting and stop alpha
 * compounding, both of which read as a rendering bug far from the cause. The rule travels with the
 * value rather than living at each call site.
 *
 * Locals are declared once at file scope, so [slot] is handed out before the first frame and reads
 * are an array index rather than a hash lookup.
 */
class UiLocal<T> internal constructor(
    internal val slot: Int,
    internal val default: T,
    internal val combine: (parent: T, incoming: T) -> T,
)

private var nextLocalSlot = 0

/**
 * Declares a local. Call at file scope -- one slot is allocated per declaration, for the process,
 * so declaring inside a function or a loop leaks slots a frame at a time.
 */
fun <T> uiLocalOf(
    default: T,
    combine: (parent: T, incoming: T) -> T = { _, incoming -> incoming },
): UiLocal<T> = UiLocal(nextLocalSlot++, default, combine)

/**
 * Per-context storage: one independent stack per local, indexed by slot.
 *
 * A single shared undo journal would be cheaper -- one array write per provide, no per-local list
 * -- but it assumes every pop is the most recent push. That does not hold here: `surface()` pushes
 * a text style, then a shape spec, and unwinds them in the order that reads best rather than in
 * strict reverse. Independent stacks are order-insensitive, which is the property the existing
 * push/pop API has always had and callers rely on without knowing it.
 */
internal class UiLocalValues {
    private var stacks = arrayOfNulls<Any?>(INITIAL_SLOTS)

    @Suppress("UNCHECKED_CAST")
    private fun <T> stackFor(local: UiLocal<T>): MutableList<T> {
        if (local.slot >= stacks.size) {
            stacks = stacks.copyOf(maxOf(local.slot + 1, stacks.size * 2))
        }
        val existing = stacks[local.slot]
        if (existing != null) return existing as MutableList<T>
        val created = mutableListOf(local.default)
        stacks[local.slot] = created
        return created
    }

    fun <T> current(local: UiLocal<T>): T = stackFor(local).last()

    fun <T> push(local: UiLocal<T>, value: T) {
        val stack = stackFor(local)
        stack.add(local.combine(stack.last(), value))
    }

    /** Keeps the base entry: an unbalanced pop is a caller bug, but draining the stack would take
     * every later reader down with it. */
    fun <T> pop(local: UiLocal<T>) {
        val stack = stackFor(local)
        if (stack.size > 1) stack.removeAt(stack.size - 1)
    }

    fun <T> reset(local: UiLocal<T>, value: T = local.default) {
        val stack = stackFor(local)
        stack.clear()
        stack.add(value)
    }

    /** Collapses every local that has ever been touched back to its default. A reused trial context
     * is never popped back the way a real widget's push/pop pair is, so it resets instead. */
    @Suppress("UNCHECKED_CAST")
    fun resetAll() {
        for (stack in stacks) {
            val list = stack as? MutableList<Any?> ?: continue
            // Entry 0 is the default the stack was seeded with, so truncating to it restores the
            // default without this class having to remember every UiLocal instance ever declared.
            while (list.size > 1) list.removeAt(list.size - 1)
        }
    }

    private companion object {
        const val INITIAL_SLOTS = 16
    }
}
