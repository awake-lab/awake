// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

data class Version(val major: Int, val minor: Int, val patch: Int) {
    companion object {

        val Version.vkVersion: Int
            get() = createVersion(major, minor, patch)

        private fun createVersion(major: Int, minor: Int, patch: Int): Int {
            require(major in 0 until 64) { "Major version must be in the range [0, 63]" }
            require(minor in 0 until 64) { "Minor version must be in the range [0, 63]" }
            require(patch in 0 until 4096) { "Patch version must be in the range [0, 4095]" }

            return (major shl 22) or (minor shl 12) or patch
        }
    }
}