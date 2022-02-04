package com.dbflow5.annotation.opts

/**
 * Description: Meant for cross-module usages that are not intended for
 * outside consumption.
 */
@RequiresOptIn(
    message = "This is intended for cross-module internal usage. Not intended for outside " +
        "consumption."
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalDBFlowApi
