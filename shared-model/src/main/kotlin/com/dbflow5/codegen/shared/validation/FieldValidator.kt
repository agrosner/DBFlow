package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.FieldModel

/**
 * Description: Collects and runs set of validations against a collection of fields.
 */
class FieldValidator {

    private val validators = listOf(
        DefaultFieldValueValidator()
    )

    @Throws(ValidationException::class)
    fun validate(fieldModel: FieldModel) {
        validators.forEach { it.validate(fieldModel) }
    }
}
