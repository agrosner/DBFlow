package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.isNotNullOrEmpty

/**
 * Description: Validates that the defaultValue is not specified
 * for [ReferenceHolderModel]
 */
class DefaultFieldValueValidator : Validator<FieldModel> {

    override fun validate(value: FieldModel) {
        if (value is ReferenceHolderModel &&
            value.properties?.defaultValue.isNotNullOrEmpty()
        ) {
            throw ValidationException(ERROR_MSG, value.name)
        }
    }

    companion object {
        const val ERROR_MSG = "Default values cannot be specified for reference field types"
    }
}
