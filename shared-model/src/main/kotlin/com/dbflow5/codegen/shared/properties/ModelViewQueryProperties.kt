package com.dbflow5.codegen.shared.properties

import com.dbflow5.codegen.shared.ClassAdapterFieldModel
import com.dbflow5.codegen.shared.NameModel

/**
 * Description: Holds the name of the field.
 */
data class ModelViewQueryProperties(
    val name: NameModel,
    /**
     * Required to use proper adapters
     */
    val adapterParams: List<ClassAdapterFieldModel>,
)