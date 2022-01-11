package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * Description:
 */
class KaptClassDeclaration(
    private val typeElement: TypeElement,
) : ClassDeclaration {

    override val isEnum: Boolean = typeElement.kind == ElementKind.ENUM

    override val properties: List<PropertyDeclaration> =
        typeElement.enclosedElements.filterIsInstance<VariableElement>().map {
            KaptPropertyDeclaration(
                it
            )
        }
}