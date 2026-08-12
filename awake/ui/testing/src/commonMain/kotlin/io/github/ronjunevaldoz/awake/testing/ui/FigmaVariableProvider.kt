// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

@Serializable
private data class DesignTokensSchema(
    val file_key: String,
    val modes: List<String>,
    val tokens: Map<String, Map<String, JsonElement>>,
)

/**
 * Loads design tokens from a synced `design-tokens.json` (see `sync-figma-variables.py`)
 * and provides type-safe access for fidelity tests.
 */
class FigmaVariableProvider private constructor(
    private val schema: DesignTokensSchema,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        suspend fun load(resourcePath: String): FigmaVariableProvider {
            val bytes = readResourceBytes(resourcePath)
            val content = bytes.decodeToString()
            val schema = json.decodeFromString<DesignTokensSchema>(content)
            return FigmaVariableProvider(schema)
        }
    }

    fun getPx(tokenId: String, mode: String = "default"): Float {
        val token = schema.tokens[tokenId] ?: error("Token '$tokenId' not found")
        val value = token[mode] ?: token.values.firstOrNull() ?: error("Mode '$mode' not found for token '$tokenId'")
        return value.jsonPrimitive.double.toFloat()
    }

    fun getColor(tokenId: String, mode: String = "default"): Color {
        val token = schema.tokens[tokenId] ?: error("Token '$tokenId' not found")
        val value = token[mode] ?: token.values.firstOrNull() ?: error("Mode '$mode' not found for token '$tokenId'")
        val hex = value.jsonPrimitive.content
        return Color.fromHex(hex)
    }

    fun availableModes(): List<String> = schema.modes
}
