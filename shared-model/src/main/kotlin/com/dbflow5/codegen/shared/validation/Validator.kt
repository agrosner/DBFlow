package com.dbflow5.codegen.shared.validation

/**
 * Description:
 */
fun interface Validator<T> {

    @Throws(ValidationException::class)
    fun validate(value: T)
}
