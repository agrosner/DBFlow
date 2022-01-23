package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.ClassType
import com.dbflow5.codegen.shared.interop.Declaration
import com.dbflow5.processor.utils.isNullable
import com.dbflow5.processor.utils.javaToKotlinType
import com.dbflow5.processor.utils.toKTypeName
import com.dbflow5.processor.utils.toTypeElement
import com.dbflow5.processor.utils.toTypeElementOrNull
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.javapoet.toJTypeName
import kotlinx.metadata.KmClass
import javax.lang.model.element.Element
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

    override fun toTypeName(): TypeName = typeMirror.toKTypeName()
        .copy(nullable = isMarkedNullable)

    override val isMarkedNullable: Boolean = element.isNullable()
}

data class KaptTypeElementKotlinClassType(
    private val typeSpec: TypeSpec,
    private val typeElement: TypeElement,
    private val kmClass: KmClass,
) : ClassType {
    // top level elements don't need nullability.
    override fun makeNotNullable(): ClassType = this

    override val declaration: Declaration = KaptKotlinDeclaration(
        typeElement, typeSpec
    )

    override fun toTypeName(): TypeName = ClassName.bestGuess(kmClass.name)

    override val isMarkedNullable: Boolean = toTypeName().isNullable
}

interface KaptVariableElementClassType : ClassType {
    val isMutable: Boolean
}

fun KaptVariableElementClassType(
    input: KaptPropertyDeclaration,
): KaptVariableElementClassType = when (input) {
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
    private val variableElement: Element,
    private val typeMirror: TypeMirror,
) : KaptVariableElementClassType {
    override fun makeNotNullable(): ClassType = this

    override val declaration: Declaration by lazy {
        val toTypeElementOrNull = typeMirror.toTypeElementOrNull()
        toTypeElementOrNull?.let {
            KaptDeclaration(typeMirror, toTypeElementOrNull)
        } ?: KaptPrimitiveDeclaration(
            variableElement,
        )
    }

    override fun toTypeName(): TypeName = variableElement.javaToKotlinType()
        .copy(nullable = isMarkedNullable)

    override val isMarkedNullable: Boolean = variableElement.isNullable()

    // TODO: infer mutability from setters?
    override val isMutable: Boolean = true
}

data class KaptVariableElementKotlinClassType(
    private val variableElement: Element,
    private val typeMirror: TypeMirror,
    private val propertySpec: PropertySpec,
) : KaptVariableElementClassType {
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
        } ?: KaptPrimitiveDeclaration(
            variableElement,
        )
    }

    override val isMutable: Boolean = propertySpec.mutable

    override fun toTypeName(): TypeName = propertySpec.type

    override val isMarkedNullable: Boolean = propertySpec.type.isNullable
}
