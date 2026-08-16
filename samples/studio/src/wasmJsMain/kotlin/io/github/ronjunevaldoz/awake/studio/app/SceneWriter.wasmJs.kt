// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement

/** The browser has no filesystem to write to, so the document leaves as a download. Encoded as a
 * data URI rather than a Blob: no object URL to revoke, and a scene document is small. */
internal actual fun writeSceneDocument(fileName: String, json: String): String {
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = "data:application/json;charset=utf-8," + encodeURIComponent(json)
    anchor.download = fileName
    anchor.click()
    return "download: $fileName"
}

// `value` reads as unused to static analysis -- it is referenced inside the js() string, which
// is the only way to call a browser global from wasmJs.
@Suppress("UnusedParameter")
private fun encodeURIComponent(value: String): String = js("encodeURIComponent(value)")
