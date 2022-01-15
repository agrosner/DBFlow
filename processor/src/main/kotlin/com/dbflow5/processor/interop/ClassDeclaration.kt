package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.OriginatingFileType
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.toTypeElement
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
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
        generateSequence(typeElement?.superclass.toTypeElement()) { prev ->
            prev.superclass.toTypeElement()
        }.map { it.asClassName() }

    override fun asStarProjectedType(): ClassDeclaration {
        return KaptClassDeclaration(
            typeElement?.let {
                ProcessorManager.manager.typeUtils
                    .erasure(it.asType()).toTypeElement()
            }
        )
    }
}
