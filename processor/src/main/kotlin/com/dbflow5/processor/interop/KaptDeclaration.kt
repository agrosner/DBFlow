package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.Declaration
import com.dbflow5.processor.utils.getPackage
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

fun KaptDeclaration(
    typeMirror: TypeMirror,
    element: TypeElement,
): Declaration {
    return safeResolveMetaData(element,
        fallback = { KaptJavaDeclaration(typeMirror, element) }) { typeSpec, _ ->
        KaptKotlinDeclaration(element, typeSpec)
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
) : Declaration {
    override val simpleName: NameModel = typeElement.name()

    override val closestClassDeclaration: ClassDeclaration? by lazy {
        KaptKotlinClassDeclaration(typeElement, typeSpec)
    }

    override fun hasValueModifier(): Boolean = typeSpec.modifiers
        .contains(KModifier.VALUE)
}

data class KaptPrimitiveDeclaration(
    private val variableElement: Element,
) : Declaration {
    override val simpleName: NameModel = NameModel(
        variableElement.simpleName,
        variableElement.getPackage(),
    )
    override val closestClassDeclaration: ClassDeclaration? = null
    override fun hasValueModifier(): Boolean = false
}
