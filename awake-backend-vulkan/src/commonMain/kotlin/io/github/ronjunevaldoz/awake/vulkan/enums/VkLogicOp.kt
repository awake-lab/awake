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

enum class VkLogicOp(val value: Int) {
    VK_LOGIC_OP_CLEAR(0),
    VK_LOGIC_OP_AND(1),
    VK_LOGIC_OP_AND_REVERSE(2),
    VK_LOGIC_OP_COPY(3),
    VK_LOGIC_OP_AND_INVERTED(4),
    VK_LOGIC_OP_NO_OP(5),
    VK_LOGIC_OP_XOR(6),
    VK_LOGIC_OP_OR(7),
    VK_LOGIC_OP_NOR(8),
    VK_LOGIC_OP_EQUIVALENT(9),
    VK_LOGIC_OP_INVERT(10),
    VK_LOGIC_OP_OR_REVERSE(11),
    VK_LOGIC_OP_COPY_INVERTED(12),
    VK_LOGIC_OP_OR_INVERTED(13),
    VK_LOGIC_OP_NAND(14),
    VK_LOGIC_OP_SET(15),
    VK_LOGIC_OP_MAX_ENUM(0x7FFFFFFF)
}