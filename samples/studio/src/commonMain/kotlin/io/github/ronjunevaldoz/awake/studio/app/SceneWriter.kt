// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

/**
 * Writes an exported scene document somewhere the user can get at it, and returns a
 * human-readable description of where it went (a path on desktop, a filename in the browser) for
 * the studio console.
 *
 * Expect/actual because there is no shared filesystem: the desktop target writes a real file, the
 * browser has none and hands the JSON to a download instead. Returning a string rather than
 * writing to a caller-chosen path keeps that difference honest -- "saved to ~/..." is a desktop
 * truth the web target cannot promise.
 */
internal expect fun writeSceneDocument(fileName: String, json: String): String
