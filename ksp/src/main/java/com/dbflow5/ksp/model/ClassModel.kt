package com.dbflow5.ksp.model

import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.TypeName

data class ClassModel(
    val name: KSName,
    /**
     * Declared type of the class.
     */
    val classType: TypeName,
    val type: ClassType,
    val fields: List<FieldModel>,
) : ObjectModel {

    sealed interface ClassType {
        object Normal : ClassType
        object View : ClassType
        object Query : ClassType
    }
}
