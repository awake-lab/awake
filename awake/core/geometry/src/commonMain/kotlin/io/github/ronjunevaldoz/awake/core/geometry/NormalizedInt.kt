// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.geometry

/**
 * The integer-to-float normalization convention GPU vertex formats (and glTF's `normalized`
 * accessor flag) share -- an integer component packed into `[-1, 1]` (signed) or `[0, 1]`
 * (unsigned) range, at whatever bit width it was stored in. Not glTF-specific: this is the
 * same convention a quantized vertex buffer uses regardless of which file format put the
 * bytes there -- a caller reading glTF's `normalized` accessor flag (or any other format with
 * the same convention) decodes through these.
 */
object NormalizedInt {
    private const val INT8_MAX = 127f
    private const val UINT8_MAX = 255f
    private const val INT16_MAX = 32767f
    private const val UINT16_MAX = 65535f

    /** [byte] in `-128..127`, decoded to `[-1, 1]` -- glTF spec: `max(c / 127.0, -1.0)`. */
    fun signedByte(byte: Int): Float = (byte / INT8_MAX).coerceAtLeast(-1f)

    /** [byte] in `0..255`, decoded to `[0, 1]`. */
    fun unsignedByte(byte: Int): Float = byte / UINT8_MAX

    /** [short] in `-32768..32767`, decoded to `[-1, 1]` -- glTF spec: `max(c / 32767.0, -1.0)`. */
    fun signedShort(short: Int): Float = (short / INT16_MAX).coerceAtLeast(-1f)

    /** [short] in `0..65535`, decoded to `[0, 1]`. */
    fun unsignedShort(short: Int): Float = short / UINT16_MAX
}
