package com.dbflow5.codegen.model.properties

import com.dbflow5.codegen.model.FieldModel
import com.dbflow5.codegen.model.IndexGroupModel
import com.squareup.kotlinpoet.ClassName

data class IndexGroupProperties(
    val number: Int,
    override val name: String,
    val unique: Boolean
) : NamedProperties {
    fun toModel(
        classType: ClassName,
        fields: List<FieldModel>
    ) = IndexGroupModel(
        name = name,
        unique = unique,
        tableTypeName = classType,
        fields = fields.filter {
            it.indexProperties?.groups?.contains(
                number
            ) == true
        }
    )
}
