package com.dbflow5.codegen.shared.parser.validation

/**
 * Description: Main validation error that happens.
 */
class ValidationException : Throwable {
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}

interface ValidationExceptionProvider {
    val message: String
    val exception: ValidationException
        get() = ValidationException(
            message
        )
}
