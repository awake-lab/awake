// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.navigation

import io.github.ronjunevaldoz.awake.scene.navigation.NavMesh

/**
 * Builds the navmesh for the scene3d playground's demo world.
 *
 * This is sample-owned on purpose: the reusable `awake:scene` module owns only the small
 * [NavMesh] contract, not this demo's hardcoded ground/obstacle geometry or recast4j
 * bootstrap.
 */
internal expect fun createScene3DDemoNavMesh(): NavMesh?
