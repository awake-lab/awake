package io.github.ronjunevaldoz.awake.engine.app.lifecycle

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.engine.app.config.WindowConfig
import io.github.ronjunevaldoz.awake.engine.app.dsl.AppServiceLookup
import kotlin.reflect.KClass

/**
 * A single game session. Pure delegation to a [AppLifecycle] implementation with
 * attached window configuration and services.
 */
class AwakeAppLifecycle internal constructor(
    private val delegate: AppLifecycle,
    val windowConfig: WindowConfig,
    private val services: Map<KClass<*>, Any>,
) : AppLifecycle by delegate,
    AppServiceLookup {

    /** The session's input accumulator. Guaranteed to exist. */
    val input: Input get() = requireService(Input::class)

    // services is keyed by the exact KClass<T> each value was registered under (see
    // MutableGameServices.register), so `as? T` matches the entry's real type or the lookup
    // legitimately returns null for an unregistered type.
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> service(type: KClass<T>): T? = services[type] as? T
}