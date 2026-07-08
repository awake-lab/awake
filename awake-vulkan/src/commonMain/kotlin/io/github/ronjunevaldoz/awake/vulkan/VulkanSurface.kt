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

/**
 * [window] is a platform-native window handle -- an `android.view.Surface` on Android, or a
 * GLFW window handle (`Long`, from `VulkanWindow.glfwCreateWindow`) on desktop. There's no
 * common supertype between the two (hence `Any`, cast internally by each actual), so callers
 * that need to be cross-platform can go through this instead of branching on `window is Long`
 * themselves.
 */
expect fun createSurface(instance: Long, window: Any): Long

/**
 * Tears down whatever platform-native window resources [window] represents, if any.
 * A no-op on Android, which owns its own `Surface`/window lifecycle; on desktop this
 * destroys the GLFW window and terminates GLFW.
 */
expect fun destroySurfaceWindow(window: Any)
