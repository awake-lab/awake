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

enum class VkStencilOp(val value: Int) {
    VK_STENCIL_OP_KEEP(0),
    VK_STENCIL_OP_ZERO(1),
    VK_STENCIL_OP_REPLACE(2),
    VK_STENCIL_OP_INCREMENT_AND_CLAMP(3),
    VK_STENCIL_OP_DECREMENT_AND_CLAMP(4),
    VK_STENCIL_OP_INVERT(5),
    VK_STENCIL_OP_INCREMENT_AND_WRAP(6),
    VK_STENCIL_OP_DECREMENT_AND_WRAP(7),
    VK_STENCIL_OP_MAX_ENUM(0x7FFFFFFF)
}