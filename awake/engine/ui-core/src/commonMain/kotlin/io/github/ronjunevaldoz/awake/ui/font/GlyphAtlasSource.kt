// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.math.floor

internal object GlyphAtlasSource {
    const val sourceCellSize = 8

    fun atlasCharFor(char: Char): Char = when {
        glyphRows.containsKey(char) -> char
        char.isLowerCase() && glyphRows.containsKey(char.uppercaseChar()) -> char.uppercaseChar()
        else -> char
    }

    fun rowsFor(char: Char): IntArray? = glyphRows[atlasCharFor(char)]

    fun advanceFor(char: Char, glyphPx: Float): Float {
        val atlasChar = atlasCharFor(char)
        val rows = glyphRows[atlasChar]
        if (rows == null) {
            return glyphPx
        }
        if (atlasChar == ' ') {
            return glyphPx * 0.5f
        }
        val occupied = occupiedColumnRange(rows) ?: return glyphPx * 0.5f
        val inkWidth = occupied.last - occupied.first + 1
        val padding = when (atlasChar) {
            'i', 'l', '.', ',', ':', ';', '!', '\'', '"' -> 1
            'm', 'M', 'w', 'W' -> 2
            else -> 2
        }
        val advanceColumns = (inkWidth + padding).coerceIn(2, sourceCellSize)
        return glyphPx * advanceColumns / sourceCellSize.toFloat()
    }

    fun isFilled(rows: IntArray, sourceX: Int, sourceY: Int): Boolean {
        if (sourceX !in 0 until sourceCellSize || sourceY !in 0 until sourceCellSize) {
            return false
        }
        val bits = rows[sourceY]
        return ((bits shr (sourceCellSize - 1 - sourceX)) and 1) == 1
    }

    fun sampleCoverage(
        rows: IntArray,
        atlasX: Int,
        atlasY: Int,
        atlasInsetPx: Int,
        atlasCellSize: Int,
        atlasInnerSize: Int,
        supersampleGrid: Int
    ): Float {
        if (atlasX < atlasInsetPx || atlasY < atlasInsetPx) return 0f
        if (atlasX >= atlasCellSize - atlasInsetPx || atlasY >= atlasCellSize - atlasInsetPx) return 0f
        var covered = 0
        var total = 0
        for (sampleY in 0 until supersampleGrid) {
            for (sampleX in 0 until supersampleGrid) {
                val sourceX = ((atlasX - atlasInsetPx) + (sampleX + 0.5f) / supersampleGrid) / atlasInnerSize * sourceCellSize
                val sourceY = ((atlasY - atlasInsetPx) + (sampleY + 0.5f) / supersampleGrid) / atlasInnerSize * sourceCellSize
                val sourceCol = floor(sourceX).toInt()
                val sourceRow = floor(sourceY).toInt()
                if (isFilled(rows, sourceCol, sourceRow)) {
                    covered += 1
                }
                total += 1
            }
        }
        return covered.toFloat() / total.toFloat()
    }

    private fun glyph(vararg rows: String): IntArray = IntArray(sourceCellSize) { rowIndex ->
        rows.getOrElse(rowIndex) { "........" }.fold(0) { bits, cell ->
            (bits shl 1) or if (cell == '#') 1 else 0
        }
    }

    private fun occupiedColumnRange(rows: IntArray): IntRange? {
        var minX = sourceCellSize
        var maxX = -1
        repeat(sourceCellSize) { y ->
            repeat(sourceCellSize) { x ->
                if (isFilled(rows, x, y)) {
                    minX = minOf(minX, x)
                    maxX = maxOf(maxX, x)
                }
            }
        }
        return if (maxX < minX) null else minX..maxX
    }

    private val glyphRows: Map<Char, IntArray> = mapOf(
        ' ' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
        '!' to glyph(
            "...##...",
            "...##...",
            "...##...",
            "...##...",
            "...##...",
            "........",
            "...##...",
            "........"
        ),
        '"' to glyph(
            "..#.#...",
            "..#.#...",
            "..#.#...",
            "........",
            "........",
            "........",
            "........",
            "........"
        ),
        '\'' to glyph(
            "...##...",
            "...##...",
            "...##...",
            "........",
            "........",
            "........",
            "........",
            "........"
        ),
        '(' to glyph(
            "...##...",
            "..##....",
            ".##.....",
            ".##.....",
            ".##.....",
            ".##.....",
            "..##....",
            "...##..."
        ),
        ')' to glyph(
            ".##.....",
            "..##....",
            "...##...",
            "...##...",
            "...##...",
            "...##...",
            "..##....",
            ".##....."
        ),
        ',' to glyph(
            "........",
            "........",
            "........",
            "........",
            "........",
            "...##...",
            "...##...",
            "..##...."
        ),
        '.' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x18, 0x18),
        '/' to glyph(
            "......##",
            ".....##.",
            "....##..",
            "...##...",
            "..##....",
            ".##.....",
            "##......",
            "........"
        ),
        '-' to intArrayOf(0x00, 0x00, 0x00, 0x7E, 0x00, 0x00, 0x00, 0x00),
        ':' to intArrayOf(0x00, 0x18, 0x18, 0x00, 0x00, 0x18, 0x18, 0x00),
        ';' to glyph(
            "........",
            "...##...",
            "...##...",
            "........",
            "........",
            "...##...",
            "...##...",
            "..##...."
        ),
        '?' to glyph(
            ".####...",
            "##..##..",
            "....##..",
            "...##...",
            "..##....",
            "........",
            "..##....",
            "........"
        ),
        '0' to intArrayOf(0x7C, 0x82, 0x86, 0x8A, 0x92, 0xA2, 0x82, 0x7C),
        '1' to intArrayOf(0x18, 0x38, 0x18, 0x18, 0x18, 0x18, 0x18, 0x7E),
        '2' to intArrayOf(0x7C, 0x82, 0x02, 0x0C, 0x30, 0x40, 0x80, 0xFE),
        '3' to intArrayOf(0xFE, 0x04, 0x08, 0x1C, 0x02, 0x02, 0x84, 0x78),
        '4' to intArrayOf(0x0C, 0x14, 0x24, 0x44, 0x84, 0xFE, 0x04, 0x04),
        '5' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x02, 0x02, 0x84, 0x78),
        '6' to intArrayOf(0x3C, 0x40, 0x80, 0xFC, 0x82, 0x82, 0x82, 0x7C),
        '7' to intArrayOf(0xFE, 0x02, 0x04, 0x08, 0x10, 0x20, 0x20, 0x20),
        '8' to intArrayOf(0x7C, 0x82, 0x82, 0x7C, 0x82, 0x82, 0x82, 0x7C),
        '9' to intArrayOf(0x7C, 0x82, 0x82, 0x82, 0x7E, 0x02, 0x04, 0x78),
        'A' to intArrayOf(0x38, 0x44, 0x82, 0x82, 0xFE, 0x82, 0x82, 0x82),
        'B' to intArrayOf(0xF8, 0x84, 0x84, 0xF8, 0x84, 0x84, 0x84, 0xF8),
        'C' to intArrayOf(0x7C, 0x82, 0x80, 0x80, 0x80, 0x80, 0x82, 0x7C),
        'D' to intArrayOf(0xF8, 0x84, 0x82, 0x82, 0x82, 0x82, 0x84, 0xF8),
        'E' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x80, 0x80, 0x80, 0xFE),
        'F' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x80, 0x80, 0x80, 0x80),
        'G' to intArrayOf(0x7C, 0x82, 0x80, 0x80, 0x8E, 0x82, 0x82, 0x7C),
        'H' to intArrayOf(0x82, 0x82, 0x82, 0xFE, 0x82, 0x82, 0x82, 0x82),
        'I' to intArrayOf(0x7C, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x7C),
        'J' to intArrayOf(0x3E, 0x04, 0x04, 0x04, 0x04, 0x04, 0x84, 0x78),
        'K' to intArrayOf(0x82, 0x84, 0x88, 0x90, 0x90, 0x88, 0x84, 0x82),
        'L' to intArrayOf(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0xFE),
        'M' to intArrayOf(0x82, 0xC6, 0xAA, 0x92, 0x82, 0x82, 0x82, 0x82),
        'N' to intArrayOf(0x82, 0xC2, 0xA2, 0x92, 0x8A, 0x86, 0x82, 0x82),
        'O' to intArrayOf(0x7C, 0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x7C),
        'P' to intArrayOf(0xF8, 0x84, 0x84, 0xF8, 0x80, 0x80, 0x80, 0x80),
        'Q' to intArrayOf(0x7C, 0x82, 0x82, 0x82, 0x82, 0x8A, 0x84, 0x7A),
        'R' to intArrayOf(0xF8, 0x84, 0x84, 0xF8, 0x90, 0x88, 0x84, 0x82),
        'S' to intArrayOf(0x7C, 0x82, 0x80, 0x7C, 0x02, 0x02, 0x82, 0x7C),
        'T' to intArrayOf(0xFE, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18),
        'U' to intArrayOf(0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x7C),
        'V' to intArrayOf(0x82, 0x82, 0x82, 0x82, 0x44, 0x44, 0x28, 0x10),
        'W' to intArrayOf(0x82, 0x82, 0x82, 0x82, 0x82, 0x92, 0xAA, 0x44),
        'X' to intArrayOf(0x82, 0x44, 0x28, 0x10, 0x10, 0x28, 0x44, 0x82),
        'Y' to intArrayOf(0x82, 0x82, 0x44, 0x28, 0x10, 0x10, 0x10, 0x10),
        'Z' to intArrayOf(0xFE, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0xFE),
        '[' to glyph(
            "..####..",
            "..##....",
            "..##....",
            "..##....",
            "..##....",
            "..##....",
            "..##....",
            "..####.."
        ),
        '\\' to glyph(
            "##......",
            ".##.....",
            "..##....",
            "...##...",
            "....##..",
            ".....##.",
            "......##",
            "........"
        ),
        ']' to glyph(
            "..####..",
            "....##..",
            "....##..",
            "....##..",
            "....##..",
            "....##..",
            "....##..",
            "..####.."
        ),
        '_' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE),
        '=' to glyph(
            "........",
            ".#####..",
            "........",
            "........",
            ".#####..",
            "........",
            "........",
            "........"
        ),
        '>' to glyph(
            "........",
            ".##.....",
            "...##...",
            ".....##.",
            "...##...",
            ".##.....",
            "........",
            "........"
        ),
        '{' to glyph(
            "...###..",
            "..##....",
            "..##....",
            ".##.....",
            "..##....",
            "..##....",
            "...###..",
            "........"
        ),
        '}' to glyph(
            ".###....",
            "...##...",
            "...##...",
            "....##..",
            "...##...",
            "...##...",
            ".###....",
            "........"
        ),
        'a' to glyph(
            "........",
            ".####...",
            ".....##.",
            ".#####..",
            "##...##.",
            "##..###.",
            ".###.##.",
            "........"
        ),
        'b' to glyph(
            "##......",
            "##......",
            "##.###..",
            "###..##.",
            "##...##.",
            "##...##.",
            "#####...",
            "........"
        ),
        'c' to glyph(
            "........",
            ".####...",
            "##..##..",
            "##......",
            "##......",
            "##..##..",
            ".####...",
            "........"
        ),
        'd' to glyph(
            ".....##.",
            ".....##.",
            ".###.##.",
            "##..###.",
            "##...##.",
            "##...##.",
            ".#####..",
            "........"
        ),
        'e' to glyph(
            "........",
            ".####...",
            "##..##..",
            "######..",
            "##......",
            "##..##..",
            ".####...",
            "........"
        ),
        'f' to glyph(
            "...###..",
            "..##....",
            ".#####..",
            "..##....",
            "..##....",
            "..##....",
            "..##....",
            "........"
        ),
        'g' to glyph(
            "........",
            ".#####..",
            "##...##.",
            "##...##.",
            ".#####..",
            ".....##.",
            ".####...",
            "........"
        ),
        'h' to glyph(
            "##......",
            "##......",
            "##.###..",
            "###..##.",
            "##...##.",
            "##...##.",
            "##...##.",
            "........"
        ),
        'i' to glyph(
            "...##...",
            "........",
            "..###...",
            "...##...",
            "...##...",
            "...##...",
            "..####..",
            "........"
        ),
        'j' to glyph(
            "....##..",
            "........",
            "....##..",
            "....##..",
            "....##..",
            "##..##..",
            ".####...",
            "........"
        ),
        'k' to glyph(
            "##......",
            "##......",
            "##..##..",
            "##.##...",
            "####....",
            "##.##...",
            "##..##..",
            "........"
        ),
        'l' to glyph(
            "..###...",
            "...##...",
            "...##...",
            "...##...",
            "...##...",
            "...##...",
            "..####..",
            "........"
        ),
        'm' to glyph(
            "........",
            "##.##...",
            "###.##..",
            "##.#.##.",
            "##.#.##.",
            "##...##.",
            "##...##.",
            "........"
        ),
        'n' to glyph(
            "........",
            "##.###..",
            "###..##.",
            "##...##.",
            "##...##.",
            "##...##.",
            "##...##.",
            "........"
        ),
        'o' to glyph(
            "........",
            ".####...",
            "##..##..",
            "##..##..",
            "##..##..",
            "##..##..",
            ".####...",
            "........"
        ),
        'p' to glyph(
            "........",
            "#####...",
            "##..##..",
            "##..##..",
            "#####...",
            "##......",
            "##......",
            "........"
        ),
        'q' to glyph(
            "........",
            ".#####..",
            "##...##.",
            "##...##.",
            ".#####..",
            ".....##.",
            ".....##.",
            "........"
        ),
        'r' to glyph(
            "........",
            "##.###..",
            "###..##.",
            "##......",
            "##......",
            "##......",
            "##......",
            "........"
        ),
        's' to glyph(
            "........",
            ".#####..",
            "##......",
            ".####...",
            ".....##.",
            "##...##.",
            ".####...",
            "........"
        ),
        't' to glyph(
            "..##....",
            "..##....",
            ".#####..",
            "..##....",
            "..##....",
            "..##.##.",
            "...###..",
            "........"
        ),
        'u' to glyph(
            "........",
            "##...##.",
            "##...##.",
            "##...##.",
            "##...##.",
            "##..###.",
            ".###.##.",
            "........"
        ),
        'v' to glyph(
            "........",
            "##...##.",
            "##...##.",
            "##...##.",
            ".##.##..",
            ".##.##..",
            "..###...",
            "........"
        ),
        'w' to glyph(
            "........",
            "##...##.",
            "##...##.",
            "##.#.##.",
            "##.#.##.",
            "###.###.",
            ".##.##..",
            "........"
        ),
        'x' to glyph(
            "........",
            "##...##.",
            ".##.##..",
            "..###...",
            "..###...",
            ".##.##..",
            "##...##.",
            "........"
        ),
        'y' to glyph(
            "........",
            "##...##.",
            "##...##.",
            ".#####..",
            ".....##.",
            "##...##.",
            ".####...",
            "........"
        ),
        'z' to glyph(
            "........",
            "######..",
            "....##..",
            "...##...",
            "..##....",
            ".##.....",
            "######..",
            "........"
        )
    )

    val chars: List<Char> = glyphRows.keys.toList()
}
