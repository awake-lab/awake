// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
/**
 * Optional per-demo contribution to [DemoCatalog]'s debug HUD -- a demo (e.g. [CubeDemo])
 * implements this to report its own state (camera position/rotation, entity counts, whatever
 * matters for that demo) without [DemoCatalog] needing any demo-specific knowledge. Not part
 * of the `Game` interface itself: most demos won't have anything worth reporting, and this
 * is purely a sample-hello-cube debugging convenience, not an engine-level contract.
 */
interface DebugReadout {
    /** Extra debug lines to show in the HUD/console readout this frame -- called once per
     * frame from [DemoCatalog], keep this cheap (no allocation-heavy formatting). */
    fun debugLines(): List<String>
}
