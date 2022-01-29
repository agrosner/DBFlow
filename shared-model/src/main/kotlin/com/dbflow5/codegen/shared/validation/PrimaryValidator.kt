package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.FieldModel

/**
 * Description:
 */
class PrimaryValidator : Validator<ClassModel> {

    override fun validate(value: ClassModel) {
        // must have 1 primary key
        if (value.type is ClassModel.Type.Normal &&
            value.primaryFields.isEmpty()
        ) {
            throw ValidationException(AT_LEAST_ONE_PRIMARY_MSG, value.name)
        }


        // assert only one autoincrement
        value.primaryFields
            .filter {
                val type = (it.fieldType as FieldModel.FieldType.Primary)
                type.isAutoIncrement
            }.takeIf { it.size > 1 }
            ?.run {
                throw ValidationException(MORE_THAN_ONE_PRIMARY_AUTO_MSG, value.name)
            }

        // assert can't mix and match
        value.primaryFields
            .partition {
                val type = (it.fieldType as FieldModel.FieldType.Primary)
                type.isAutoIncrement || type.isRowId
            }
            .let { (ai, regular) ->
                if (ai.isNotEmpty() && regular.isNotEmpty()) {
                    throw ValidationException(MIX_AND_MATCH_PRIMARY_MSG, value.name)
                }
            }
    }

    companion object {
        const val AT_LEAST_ONE_PRIMARY_MSG = "Tables must have a least one defined primary key."

        const val MORE_THAN_ONE_PRIMARY_AUTO_MSG =
            "Only 1 autoincrementing key is allowed on a table."

        const val MIX_AND_MATCH_PRIMARY_MSG = "You cannot mix and " +
            "match autoincrement with regular primary keys."
    }
}