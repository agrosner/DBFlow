package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.FieldModel

class ClassToFieldValidator(
    private val fieldValidator: FieldValidator,
) : Validator<ClassModel> {
    @Throws(ValidationException::class)
    override fun validate(value: ClassModel) {
        value.fields.forEach { fieldValidator.validate(it) }
    }
}

/**
 * Description: Collection of field checks.
 */
class FieldValidator : GroupedValidator<FieldModel>(
    listOf(DefaultFieldValueValidator())
)
