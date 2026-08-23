package com.dbflow5.adapter

/**
 * Description: Result exception when an operation fails.
 */
class SaveOperationFailedException(
    val operation: String
) : Throwable(
    "$operation failed."
)
