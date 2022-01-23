package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.ClassType
import com.dbflow5.codegen.shared.interop.Declaration
import com.dbflow5.processor.utils.javaToKotlinType
import com.dbflow5.processor.utils.toTypeElement
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.javapoet.toJTypeName
import kotlinx.metadata.KmClass
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

fun KaptTypeElementClassType(
    typeMirror: TypeMirror,
    element: TypeElement,
): ClassType {
    return safeResolveMetaData(element, {
        KaptTypeElementJavaClassType(typeMirror, element)
    }) { typeSpec, kmClass ->
        KaptTypeElementKotlinClassType(typeSpec, element, kmClass)
    }
}

/**
 * Description:
 */
data class KaptTypeElementJavaClassType(
    private val typeMirror: TypeMirror,
    private val element: TypeElement,
) : ClassType {

    // top level elements don't need nullability.
    override fun makeNotNullable(): ClassType = this

    override val declaration: Declaration =
        KaptJavaDeclaration(typeMirror, element)

    override fun toTypeName(): TypeName = element.javaToKotlinType()

    // TODO: use annotation inference to try to gauge.
    override val isMarkedNullable: Boolean = false
}

data class KaptTypeElementKotlinClassType(
    private val typeSpec: TypeSpec,
    private val typeElement: TypeElement,
    private val kmClass: KmClass,
) : ClassType {
    // top level elements don't need nullability.
    override fun makeNotNullable(): ClassType = this

    override val declaration: Declaration = KaptKotlinDeclaration(
        typeElement, typeSpec, kmClass
    )

    override fun toTypeName(): TypeName = ClassName.bestGuess(kmClass.name)

    override val isMarkedNullable: Boolean = toTypeName().isNullable
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
        val type = variableElement.asType()
        val toTypeElementOrNull = propertySpec.type
            .toJTypeName().toTypeElement()
        toTypeElementOrNull?.let {
            KaptDeclaration(type, toTypeElementOrNull)
        } ?: KaptPrimitiveKotlinDeclaration(
            variableElement,
            propertySpec,
        )
    }

    override fun toTypeName(): TypeName = propertySpec.type

    override val isMarkedNullable: Boolean = propertySpec.type.isNullable
}
