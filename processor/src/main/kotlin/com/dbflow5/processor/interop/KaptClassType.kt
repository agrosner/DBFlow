package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.ClassType
import com.dbflow5.codegen.shared.interop.Declaration
import com.dbflow5.processor.utils.javaToKotlinType
import com.dbflow5.processor.utils.toTypeElement
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
data class KaptTypeElementClassType(
    private val typeMirror: TypeMirror,
    private val element: TypeElement,
) : ClassType {

    // top level elements don't need nullability.
    override fun makeNotNullable(): ClassType = this

    override val declaration: Declaration =
        KaptDeclaration(typeMirror, element)

    override fun toTypeName(): TypeName = element.javaToKotlinType()

    // TODO: use annotation inference to try to gauge.
    override val isMarkedNullable: Boolean = false
}

fun KaptVariableElementClassType(
    input: KaptPropertyDeclaration,
): ClassType = when (input) {
    is KaptJavaPropertyDeclaration -> {
        KaptVariableElementJavaClassType(
            input.element,
            input.element.asType(),
        )
    }
    is KaptKotlinPropertyDeclaration ->
        KaptVariableElementKotlinClassType(
            input.element,
            input.element.asType(),
            input.propertySpec,
        )
}

data class KaptVariableElementJavaClassType(
    private val variableElement: VariableElement,
    private val typeMirror: TypeMirror,
) : ClassType {
    override fun makeNotNullable(): ClassType = this

    override val declaration: Declaration by lazy {
        val mirror = variableElement.enclosingElement.asType()
        KaptDeclaration(
            mirror, mirror.toTypeElement(),
        )
    }

    override fun toTypeName(): TypeName = variableElement.javaToKotlinType()

    // TODO: infer nullability annotations
    override val isMarkedNullable: Boolean = false
}

data class KaptVariableElementKotlinClassType(
    private val variableElement: VariableElement,
    private val typeMirror: TypeMirror,
    private val propertySpec: PropertySpec,
) : ClassType {
    override fun makeNotNullable(): ClassType = KaptVariableElementKotlinClassType(
        variableElement,
        typeMirror,
        propertySpec.toBuilder(type = propertySpec.type.copy(nullable = false))
            .build(),
    )

    override val declaration: Declaration by lazy {
        val mirror = variableElement.enclosingElement.asType()
        KaptDeclaration(mirror, mirror.toTypeElement())
    }

    override fun toTypeName(): TypeName = propertySpec.type

    override val isMarkedNullable: Boolean = propertySpec.type.isNullable
}
