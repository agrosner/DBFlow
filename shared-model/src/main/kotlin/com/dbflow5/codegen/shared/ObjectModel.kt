package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
sealed interface ObjectModel {
    val originatingSource: OriginatingSource?
}

interface GeneratedClassModel {

    /**
     * The class type of object generated.
     */
    val generatedClassName: NameModel

    /**
     * The name of the generated field.
     */
    val generatedFieldName: String

    /**
     * The super class, such as ModelAdapter of what it extends.
     */
    val generatedSuperClass: TypeName
}