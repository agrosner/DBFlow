package com.dbflow5.model.properties

import com.dbflow5.model.NameModel

/**
 * Description: Holds the name of the field.
 */
data class ModelViewQueryProperties(
    val name: NameModel,
    val isProperty: Boolean,
)