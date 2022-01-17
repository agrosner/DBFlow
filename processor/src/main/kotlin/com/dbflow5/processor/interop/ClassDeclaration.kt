package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.OriginatingFileType
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.asStarProjectedType
import com.dbflow5.processor.utils.toTypeElement
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
data class KaptClassDeclaration(
    internal val typeElement: TypeElement?,
) : ClassDeclaration {

    override val isEnum: Boolean = typeElement?.kind == ElementKind.ENUM

    override val properties: List<PropertyDeclaration> =
        propertyElements
            ?.map {
                KaptPropertyDeclaration(
                    it
                )
            } ?: emptyList()

    val propertyElements: List<VariableElement>?
        get() = typeElement?.enclosedElements
            ?.filterIsInstance<VariableElement>()

    /**
     * KAPT doesn't need to track these, so we leave null.
     */
    override val containingFile: OriginatingFileType? = null

    override val superTypes: Sequence<TypeName> =
        superElements.asSequence()
            .map { it.asTypeName() }

    val superElements: List<TypeMirror>
        get() = typeElement?.asType()?.let {
            ProcessorManager.manager.typeUtils.directSupertypes(
                it
            )
        } ?: emptyList()

    override fun asStarProjectedType(): ClassDeclaration {
        return KaptClassDeclaration(
            typeElement?.asType()?.asStarProjectedType()?.toTypeElement()
        )
    }
}
