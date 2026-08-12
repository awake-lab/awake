// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

/**
 * Loads a visual baseline from the snapshot storage.
 */
expect fun loadAwakeUiSnapshot(id: String): ByteArray?

/**
 * Saves a new visual baseline to the snapshot storage.
 */
expect fun saveAwakeUiSnapshot(id: String, pixels: ByteArray, width: Int, height: Int)

/**
 * Saves a diff image highlighting regressions.
 */
expect fun saveAwakeUiDiff(id: String, pixels: ByteArray, width: Int, height: Int)
