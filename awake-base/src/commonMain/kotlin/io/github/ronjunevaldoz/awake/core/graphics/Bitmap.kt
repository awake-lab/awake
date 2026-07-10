package io.github.ronjunevaldoz.awake.core.graphics

interface Bitmap {
    val width: Int
    val height: Int
    val channel: Int
    val pixels: IntArray
}

class DefaultBitmap(
    override val width: Int,
    override val height: Int,
    override val channel: Int,
    override val pixels: IntArray
) : Bitmap

expect fun createBitmap(bytes: ByteArray): Bitmap