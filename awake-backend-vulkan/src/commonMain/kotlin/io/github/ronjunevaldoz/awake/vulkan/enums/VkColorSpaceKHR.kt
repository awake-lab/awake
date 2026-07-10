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

package io.github.ronjunevaldoz.awake.vulkan.enums

enum class VkColorSpaceKHR(val value: Int) {
    VK_COLOR_SPACE_SRGB_NONLINEAR_KHR(0),
    VK_COLOR_SPACE_DISPLAY_P3_NONLINEAR_EXT(1000104001),
    VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT(1000104002),
    VK_COLOR_SPACE_DISPLAY_P3_LINEAR_EXT(1000104003),
    VK_COLOR_SPACE_DCI_P3_NONLINEAR_EXT(1000104004),
    VK_COLOR_SPACE_BT709_LINEAR_EXT(1000104005),
    VK_COLOR_SPACE_BT709_NONLINEAR_EXT(1000104006),
    VK_COLOR_SPACE_BT2020_LINEAR_EXT(1000104007),
    VK_COLOR_SPACE_HDR10_ST2084_EXT(1000104008),
    VK_COLOR_SPACE_DOLBYVISION_EXT(1000104009),
    VK_COLOR_SPACE_HDR10_HLG_EXT(1000104010),
    VK_COLOR_SPACE_ADOBERGB_LINEAR_EXT(1000104011),
    VK_COLOR_SPACE_ADOBERGB_NONLINEAR_EXT(1000104012),
    VK_COLOR_SPACE_PASS_THROUGH_EXT(1000104013),
    VK_COLOR_SPACE_EXTENDED_SRGB_NONLINEAR_EXT(1000104014),
    VK_COLOR_SPACE_DISPLAY_NATIVE_AMD(1000213000),
    VK_COLORSPACE_SRGB_NONLINEAR_KHR(VK_COLOR_SPACE_SRGB_NONLINEAR_KHR.value),
    VK_COLOR_SPACE_DCI_P3_LINEAR_EXT(VK_COLOR_SPACE_DISPLAY_P3_LINEAR_EXT.value),
    VK_COLOR_SPACE_MAX_ENUM_KHR(0x7FFFFFFF)
}