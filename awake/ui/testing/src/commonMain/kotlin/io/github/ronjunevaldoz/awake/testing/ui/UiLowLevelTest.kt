// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

/**
 * Documents a test where manual frame lifecycle is the behavior being verified.
 *
 * Normal component fixtures use [renderUiComponent] or [UiTestSession] instead. The reason is
 * required so a reviewer can distinguish an intentional Core/runtime or renderer test from
 * copied setup boilerplate.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
annotation class UiLowLevelTest(val reason: String)
