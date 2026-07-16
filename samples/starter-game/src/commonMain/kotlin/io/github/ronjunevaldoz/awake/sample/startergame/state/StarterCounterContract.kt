// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.state

internal object StarterCounterContract {
    data class State(
        val count: Int = 0
    )

    sealed interface Intent {
        data object Increment : Intent
        data object Decrement : Intent
        data object Reset : Intent
    }

    sealed interface Effect {
        data class MilestoneReached(val count: Int) : Effect
        data object ResetCompleted : Effect
    }
}
