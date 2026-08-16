// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import java.io.File

/** Writes next to the process, not into the source tree: a sample must not silently overwrite
 * the authored `.scene.json` it shipped with. */
internal actual fun writeSceneDocument(fileName: String, json: String): String {
    val outDir = File(System.getProperty("user.home"), "Awake Studio").apply { mkdirs() }
    val file = File(outDir, fileName)
    file.writeText(json)
    return file.absolutePath
}
