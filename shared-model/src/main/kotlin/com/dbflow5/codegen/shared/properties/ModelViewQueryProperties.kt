package com.dbflow5.codegen.shared.properties

import com.dbflow5.codegen.shared.NameModel

/**
 * Description: Holds the name of the field.
 */
data class ModelViewQueryProperties(
    val name: NameModel,
    val isProperty: Boolean,
)