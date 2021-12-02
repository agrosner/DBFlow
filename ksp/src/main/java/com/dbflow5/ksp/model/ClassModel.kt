package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.ClassProperties
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.ClassName

data class ClassModel(
    val name: KSName,
    /**
     * Declared type of the class.
     */
    val classType: ClassName,
    val type: ClassType,
    val properties: ClassProperties,
    val fields: List<FieldModel>,
) : ObjectModel {

    sealed interface ClassType {
        object Normal : ClassType
        object View : ClassType
        object Query : ClassType
    }
}
