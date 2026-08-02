// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

/**
 * Debug/test-only consistency check for [resolveHasWeightedChild]'s opt-in cross-frame cache
 * (see `UiScope.row()`/`UiScope.column()`'s `id`/`cacheKey` params and
 * docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md). Off by default -- zero cost in
 * normal builds -- mirroring [UiMeasureTrialStats.enabled]'s same zero-cost-when-disabled
 * contract. Flip this on in a test to catch a caller-supplied `cacheKey` that didn't actually
 * change when the content's `.weight()`-usage structure did: every cache *hit* re-runs the real
 * trial anyway and throws on mismatch instead of silently returning the stale answer.
 */
object UiWeightCacheConsistencyCheck {
    var enabled: Boolean = false
}

/**
 * Backs `UiScope.row()`/`UiScope.column()`'s opt-in `id`/`cacheKey` cache for the
 * `hasWeightedChild` trial-detection boolean -- never sizes/positions, see the design doc for why.
 * Namespaced like `UiAnimation.kt`'s `animateFloat`'s `"__animation__$id"` convention, and
 * stored in the same real widget-state store `rememberStateValue` uses, so its
 * lifetime matches every other piece of persisted UI state (reset on [UiContext] recreation, not
 * per-frame).
 *
 * [id]/[cacheKey] are both required non-null to use the cache at all -- when either is null this
 * is a plain passthrough to [trial], byte-for-byte the pre-existing unconditional-trial behavior.
 */
internal fun UiContext.resolveHasWeightedChild(
    id: String?,
    cacheKey: Any?,
    trial: () -> Boolean
): Boolean {
    if (id == null || cacheKey == null) return trial()
    val state = widgetStateInternal("__weightcache__$id")
    val storedKey = state.get<Any?>("cacheKey", null)
    val storedValue = state.get("hasWeightedChild", false)
    if (storedKey == cacheKey) {
        if (UiWeightCacheConsistencyCheck.enabled) {
            val fresh = trial()
            check(fresh == storedValue) {
                "UiScope.row()/column() weight cache for id=\"$id\" returned stale " +
                    "hasWeightedChild=$storedValue but a fresh trial computed $fresh -- cacheKey " +
                    "($cacheKey) did not change even though the content's weight()-usage " +
                    "structure did. Fix: make cacheKey depend on whatever changed."
            }
        }
        return storedValue
    }
    val fresh = trial()
    state.set("cacheKey", cacheKey)
    state.set("hasWeightedChild", fresh)
    return fresh
}
