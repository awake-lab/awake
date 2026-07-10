package io.github.ronjunevaldoz.awake.core.shader

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Attribute(private val name: String = "", private val prefix: String = "a_") :
    ReadWriteProperty<DefaultShader, Int> {

    override fun getValue(thisRef: DefaultShader, property: KProperty<*>): Int {
        val variableName = name.ifEmpty { property.name }
        val key = if (variableName.startsWith(prefix)) variableName else property.buildKey(prefix)
        return thisRef.aBinder.getLocation(key)
    }

    override fun setValue(thisRef: DefaultShader, property: KProperty<*>, value: Int) {
        val variableName = name.ifEmpty { property.name }
        val key = if (variableName.startsWith(prefix)) variableName else property.buildKey(prefix)
        thisRef.aBinder.bind(key, value)
    }
}


fun KProperty<*>.buildKey(prefix: String): String {
    return prefix + name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}