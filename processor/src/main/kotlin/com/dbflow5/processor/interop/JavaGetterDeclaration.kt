package com.dbflow5.processor.interop

import com.dbflow5.processor.utils.simpleString
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

/**
 * Description: Simple logic wrapper.
 */
data class JavaGetterDeclaration(val element: ExecutableElement) {
    val propertyNameString = element.simpleString
        .replaceFirst("get", "")
        .replaceFirstChar { it.lowercase() }

    val simpleName = element.name()

    val propertyName = simpleName.copy(
        shortName = propertyNameString,
    )

    val isAbstract = element.modifiers.contains(Modifier.ABSTRACT)

    val returnTypeName = element.returnType.asTypeName()
}
