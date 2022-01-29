package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.FieldModel

/**
 * Description:
 */
class PrimaryValidator : Validator<ClassModel> {

    override fun validate(value: ClassModel) {
        // assert only one autoincrement
        value.primaryFields
            .filter {
                val type = (it.fieldType as FieldModel.FieldType.Primary)
                type.isAutoIncrement
            }.takeIf { it.size > 1 }
            ?.run {
                throw ValidationException("$MORE_THAN_ONE_PRIMARY_AUTO_MSG from ${value.name.print()}")
            }

        // assert can't mix and match
        value.primaryFields
            .partition {
                val type = (it.fieldType as FieldModel.FieldType.Primary)
                type.isAutoIncrement || type.isRowId
            }
            .let { (ai, regular) ->
                if (ai.isNotEmpty() && regular.isNotEmpty()) {
                    throw ValidationException(
                        "$MIX_AND_MATCH_PRIMARY_MSG from ${value.name.print()}"
                    )
                }
            }
    }

    companion object {
        const val MORE_THAN_ONE_PRIMARY_AUTO_MSG =
            "Only 1 autoincrementing key is allowed on a table."

        const val MIX_AND_MATCH_PRIMARY_MSG = "You cannot mix and " +
            "match autoincrement with regular primary keys."
    }
}