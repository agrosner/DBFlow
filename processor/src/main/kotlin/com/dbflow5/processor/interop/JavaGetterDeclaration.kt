package com.dbflow5.processor.interop

import com.dbflow5.processor.utils.simpleString
import javax.lang.model.element.ExecutableElement

/**
 * Description: Simple logic wrapper.
 */
data class JavaGetterDeclaration(val element: ExecutableElement) {
    val propertyName = element.simpleString
        .replaceFirst("get", "")
        .replaceFirstChar { it.lowercase() }
}
