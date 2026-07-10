/*
 * Awake
 * Awake.awake-vulkan.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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