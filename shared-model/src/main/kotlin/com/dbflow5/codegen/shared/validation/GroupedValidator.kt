package com.dbflow5.codegen.shared.validation

/**
 * Description: Collects and runs set of validations against a collection of fields.
 */
abstract class GroupedValidator<T>(
    private val validators: List<Validator<T>>,
): Validator<T> {
    @Throws(ValidationException::class)
    override fun validate(value: T) {
        validators.forEach { it.validate(value) }
    }
}
