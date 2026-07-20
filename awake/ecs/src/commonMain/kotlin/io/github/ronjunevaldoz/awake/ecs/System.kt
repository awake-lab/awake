// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

/**
 * An ECS system that operates on entities and components in a [World].
 */
interface System {
    /**
     * Defines how often this system should be updated. Defaults to [SystemFrequency.Simulation].
     */
    val frequency: SystemFrequency get() = SystemFrequency.Simulation

    /**
     * Called once per frame or fixed step depending on [frequency].
     */
    fun update(world: World, delta: Float)
}

/**
 *
 * TODO analyze this proposal, this recommendation might be wrong, do a deep research and analysis first
 *
 * Act as an expert Systems Engineer and Game Engine Architect specializing in high-performance Data-Oriented Design (DOD). I am building a custom, high-performance, single-threaded Entity Component System (ECS) framework in Kotlin Multiplatform Mobile (KMM).
 *
 * The core engine layout is functional and currently BEATING established libraries in baseline performance benchmarks. I need you to evaluate its technical footprint, preserve its hyper-optimized execution metrics, and help design a safe, incremental optimization path that transitions it to an Eager, Push-Based Reactive Architecture while cleanly incorporating Kotlin Coroutines for async system boundaries.
 *
 * ### EXECUTING CODE CONTEXT:
 * 1. Fully Decoupled System & Manager Interface: Systems are completely stateless, zero-allocation processing blocks that do not manage their own timing or frequency. A dedicated `SystemManager` buckets systems into raw arrays by execution interval to avoid branch mispredictions in the main loop:
 *    data class Query(val aspect: Aspect)
 *    interface System {
 *        val query: Query
 *        fun update(entities: LongArray, size: Int, delta: Float)
 *    }
 * 2. Entities: A value class inline wrapper (`value class Entity(val packed: Long)`) holding a 32-bit ID and 32-bit generation to completely eliminate primitive-to-object boxing.
 * 3. Component Stores: A Sparse Set layout (`ComponentStore<T>`) backed by primitive arrays (`LongArray` for dense entities, `Array<Any?>` for dense components) and tracked via a custom `EntityIndexMap` containing a raw `IntArray`. It relies on a swap-and-pop array shift mechanics during removals to maintain dense array contiguity.
 * 4. System Queries: Uses a `QueryCache` with a lazy global invalidation system relying on basic integer version stamping (`typedQueryVersion`). Any structural change anywhere in the world triggers `queryCache.markAllQueriesDirty()`, dropping all system views and forcing full-world linear filter scans (`QueryCollector.collect`) on the next frame tick.
 * 5. Parallel Tracking Layer: A `FamilyRegistry` coexists alongside the query cache, tracking structural mutations natively (`addComponent`, `removeComponent`) and sorting them into views like `Family2Cache<A, B>` using an unboxed list table (`arrayOfNulls<MutableList<FamilyCache>>`) indexed by component type IDs.
 *
 * ### ARCHITECTURAL GOALS & REFACTORING CONSTRAINTS:
 * - Remove `QueryCache` and `QueryCollector` entirely, replacing them with a purely reactive push layout inside `FamilyRegistry` to achieve true O(1) query lookups that stream raw primitive data directly to the refactored `System.update` calls.
 * - Keep the public `World` API facade identical so existing user-facing code doesn't break during refactoring.
 * - Eradicate implicit collection allocations and GC pressure in hot execution paths by dropping standard JVM types (like `List` and `Set`) inside internal cache layers in favor of primitive `LongArray` segments.
 * - Formulate the design for a single-threaded main loop to maintain perfect cache locality and predictable prediction paths, but explicitly address how custom async systems (e.g., Asset Loading, Network I/O) should leverage Kotlin Coroutines via dedicated background dispatchers to safely pass data back to the single-threaded `World`.
 *
 * ### CRITICAL BENCHMARK PRESERVATION CONSTRAINT:
 * Because the current stable baseline of this ECS is already winning benchmarks, the inline optimizations, custom primitive index mappings, and sparse set contiguity are highly tuned for the compiler. Any refactoring work must not compromise this performance edge. We are looking for incremental optimizations specifically targeting high-churn structural changes without introducing branch mispredictions, regression penalties, or memory overhead. Before proposing code changes, analyze the micro-architectural trade-offs compared to the current stable baseline, and outline how to safely validate them using a profiling framework (like JMH).
 *
 * Based on these precise performance parameters, provide an in-depth summary of the framework's existing flaws, followed by a step-by-step refactoring blueprint detailing how to implement the bitmask tracking layers, wire up the SystemManager execution arrays, and establish coroutine boundaries cleanly.
 *
 */