package com.dbflow5.ksp.parser.validation

/**
 * Description: Main validation error that happens.
 */
sealed class ValidationException : Throwable {
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}
