package com.dbflow5.ksp.model.properties

import com.dbflow5.ksp.model.NameModel

/**
 * Description: Holds the name of the field.
 */
data class ModelViewQueryProperties(
    val name: NameModel,
    val isProperty: Boolean,
)