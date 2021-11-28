package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class DatabaseModel(
    val name: KSName,
    val classType: TypeName,
    val properties: DatabaseProperties,
) : ObjectModel {

}