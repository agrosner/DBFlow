package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.ClassProperties
import com.dbflow5.ksp.model.properties.NamedProperties
import com.dbflow5.quoteIfNeeded
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

    val primaryFields = fields.filter { it.fieldType is FieldModel.FieldType.PrimaryAuto }

    /**
     * Name to use on the database.
     */
    val dbName = when (properties) {
        is NamedProperties -> properties.nameWithFallback(name.getShortName())
        else -> name.getShortName()
    }.quoteIfNeeded()

    sealed interface ClassType {
        object Normal : ClassType
        object View : ClassType
        object Query : ClassType
    }
}
