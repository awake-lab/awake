// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import java.io.File

private val tutorialManifestLock = Any()

fun recordGameDslTutorial(
    name: String,
    title: String,
    summary: String,
    snippet: String
) {
    synchronized(tutorialManifestLock) {
        val manifest = File("build/game-dsl-tutorials/tutorials.tsv")
        manifest.parentFile.mkdirs()
        val escapedTitle = title.replace('\t', ' ').replace('\n', ' ')
        val escapedSummary = summary.replace('\t', ' ').replace('\n', ' ')
        val escapedSnippet = snippet
            .trimIndent()
            .replace("\r\n", "\n")
            .replace('\t', ' ')
            .replace('\n', '\u000B')
        val line = listOf(name, escapedTitle, escapedSummary, escapedSnippet).joinToString("\t")
        val entries = linkedMapOf<String, String>()
        if (manifest.exists()) {
            manifest.readLines()
                .filter { it.isNotBlank() }
                .forEach { existing ->
                    entries[existing.substringBefore('\t')] = existing
                }
        }
        entries[name] = line
        manifest.writeText(entries.values.joinToString(separator = "\n", postfix = "\n"))
    }
}
