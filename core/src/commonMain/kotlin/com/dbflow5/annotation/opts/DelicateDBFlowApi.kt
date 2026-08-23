package com.dbflow5.annotation.opts

/**
 * Description: Marks a particular part of DBFlow as being slightly more advanced.
 *
 * These pieces of API may change, but could be used outside of the library.
 */
@RequiresOptIn(
    message = "This API is more advanced and can be prone to error.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DelicateDBFlowApi
