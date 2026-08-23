package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.NameModel

/**
 * Description: Main validation error that happens.
 */
class ValidationException : Throwable {
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String, name: NameModel) : super("$message from: ${name.print()}")
}

interface ValidationExceptionProvider {
    val message: String
    val exception: ValidationException
        get() = ValidationException(
            message
        )
}
