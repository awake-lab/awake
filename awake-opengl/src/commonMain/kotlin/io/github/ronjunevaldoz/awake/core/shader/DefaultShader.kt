package io.github.ronjunevaldoz.awake.core.shader

import kotlin.properties.PropertyDelegateProvider

abstract class DefaultShader : BaseShader() {
    val aBinder: AttributeBinder by lazy { AttributeBinderImpl(program) }
    val uBinder: UniformBinder by lazy { UniformBinderImpl(program) }
    val attribute = PropertyDelegateProvider { _: Any?, _ ->
        Attribute()
    }
    val uniform = PropertyDelegateProvider { _: Any?, _ ->
        Uniform()
    }

    var position by attribute
    var color by attribute
    var texCoords by attribute

    override fun compile() {
        super.compile()
        bindAttributes()
    }

    private fun bindAttributes() {
        position = 0
        color = 1
        texCoords = 2
    }
}