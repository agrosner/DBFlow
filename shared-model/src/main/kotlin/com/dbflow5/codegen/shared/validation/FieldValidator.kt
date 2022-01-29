package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.FieldModel

class ClassToFieldValidator(
    private val fieldValidator: FieldValidator,
) : Validator<ClassModel> {
    @Throws(ValidationException::class)
    override fun validate(value: ClassModel) {
        if (value.fields.isEmpty()) {
            throw ValidationException(EMPTY_FIELDS_MSG, value.name)
        }

        value.fields.forEach { fieldValidator.validate(it) }
    }

    companion object {
        const val EMPTY_FIELDS_MSG = "Table needs to define at least one column."
    }
}

/**
 * Description: Collection of field checks.
 */
class FieldValidator : GroupedValidator<FieldModel>(
    listOf(DefaultFieldValueValidator())
)
