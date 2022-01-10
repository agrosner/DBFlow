package com.dbflow5.codegen.model.properties

import com.dbflow5.codegen.model.NameModel

/**
 * Description: Holds the name of the field.
 */
data class ModelViewQueryProperties(
    val name: NameModel,
    val isProperty: Boolean,
)