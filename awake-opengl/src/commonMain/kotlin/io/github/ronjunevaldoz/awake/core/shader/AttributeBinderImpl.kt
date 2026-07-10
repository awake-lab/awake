package io.github.ronjunevaldoz.awake.core.shader

import io.github.aakira.napier.Napier
import io.github.ronjunevaldoz.awake.core.AwakeContext

class AttributeBinderImpl(private val programId: Int) : AttributeBinder {
    private val attributes: MutableMap<String, Int> = mutableMapOf()
    override fun bind(name: String, location: Int) {
        val attributeLocation = AwakeContext.gl.getAttribLocation(programId, name)
        if (attributeLocation != -1) {
            Napier.w("Attribute `$name`, location is explicitly specified using layout qualifier.")
            // Attribute location is explicitly specified using layout qualifier
            attributes[name] = attributeLocation
        } else {
            // Attribute location is not explicitly specified using layout qualifier
            attributes[name] = location
            AwakeContext.gl.bindAttribLocation(programId, location, name)
        }
    }

    override fun getLocation(name: String): Int {
        return checkNotNull(attributes[name]) {
            "Attribute location not found: `$name`"
        }
    }
}