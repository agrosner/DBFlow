package com.dbflow5.codegen.shared.properties

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.UniqueGroupModel

data class UniqueGroupProperties(
    val number: Int,
    val conflictAction: ConflictAction,
) {
    fun toModel(fields: List<FieldModel>) = UniqueGroupModel(
        number = number,
        conflictAction = conflictAction,
        fields = fields.filter {
            it.uniqueProperties?.groups?.contains(
                number
            ) == true
        }
    )
}
