package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.Declaration
import com.dbflow5.processor.utils.getPackage
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.metadata.KmClass
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

fun KaptDeclaration(
    typeMirror: TypeMirror,
    element: TypeElement,
): Declaration {
    return safeResolveMetaData(element,
        fallback = { KaptJavaDeclaration(typeMirror, element) }) { typeSpec, kmClass ->
        KaptKotlinDeclaration(element, typeSpec, kmClass)
    }
}

data class KaptJavaDeclaration(
    private val typeMirror: TypeMirror,
    private val element: TypeElement,
) : Declaration {
    override val simpleName: NameModel = element.name()

    /**
     * way kapt works, this is the same.
     */
    override val closestClassDeclaration: ClassDeclaration? by lazy {
        KaptJavaClassDeclaration(element)
    }

    override fun hasValueModifier(): Boolean {
        return false // kapt has no clue
    }
}

data class KaptKotlinDeclaration(
    private val typeElement: TypeElement,
    private val typeSpec: TypeSpec,
    private val kmClass: KmClass,
) : Declaration {
    override val simpleName: NameModel = typeElement.name()

    override val closestClassDeclaration: ClassDeclaration? by lazy {
        KaptKotlinClassDeclaration(typeElement, typeSpec, kmClass)
    }

    override fun hasValueModifier(): Boolean = typeSpec.modifiers
        .contains(KModifier.VALUE)
}

data class KaptPrimitiveKotlinDeclaration(
    private val variableElement: VariableElement,
    private val propertySpec: PropertySpec,
) : Declaration {
    override val simpleName: NameModel = NameModel(
        variableElement.simpleName,
        variableElement.getPackage(),
    )
    override val closestClassDeclaration: ClassDeclaration? = null
    override fun hasValueModifier(): Boolean = false
}