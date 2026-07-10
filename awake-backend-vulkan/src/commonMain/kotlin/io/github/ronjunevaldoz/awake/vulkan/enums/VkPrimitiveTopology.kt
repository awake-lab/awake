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

enum class VkPrimitiveTopology(val value: Int) {
    VK_PRIMITIVE_TOPOLOGY_POINT_LIST(0),
    VK_PRIMITIVE_TOPOLOGY_LINE_LIST(1),
    VK_PRIMITIVE_TOPOLOGY_LINE_STRIP(2),
    VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST(3),
    VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP(4),
    VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN(5),
    VK_PRIMITIVE_TOPOLOGY_LINE_LIST_WITH_ADJACENCY(6),
    VK_PRIMITIVE_TOPOLOGY_LINE_STRIP_WITH_ADJACENCY(7),
    VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST_WITH_ADJACENCY(8),
    VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP_WITH_ADJACENCY(9),
    VK_PRIMITIVE_TOPOLOGY_PATCH_LIST(10),
    VK_PRIMITIVE_TOPOLOGY_MAX_ENUM(0x7FFFFFFF)
}
