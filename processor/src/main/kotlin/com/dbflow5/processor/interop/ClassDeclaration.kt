package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.OriginatingFileType
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.asStarProjectedType
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * Description:
 */
class KaptClassDeclaration(
    private val typeElement: TypeElement?,
) : ClassDeclaration {

    override val isEnum: Boolean = typeElement?.kind == ElementKind.ENUM

    override val properties: List<PropertyDeclaration> =
        typeElement?.enclosedElements
            ?.filterIsInstance<VariableElement>()
            ?.map {
                KaptPropertyDeclaration(
                    it
                )
            } ?: emptyList()

    /**
     * KAPT doesn't need to track these, so we leave null.
     */
    override val containingFile: OriginatingFileType? = null

    override val superTypes: Sequence<TypeName> =
        ProcessorManager.manager.typeUtils.directSupertypes(
            typeElement?.asType()
        ).asSequence()
            .map { it.asTypeName() }

    override fun asStarProjectedType(): ClassDeclaration {
        return KaptClassDeclaration(typeElement?.asStarProjectedType())
    }
}
