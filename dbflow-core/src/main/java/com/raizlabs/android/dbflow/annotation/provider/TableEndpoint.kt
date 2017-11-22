package com.raizlabs.android.dbflow.annotation.provider

import kotlin.reflect.KClass

/**
 * Description: Defines an endpoint that gets placed inside of a [ContentProvider]
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class TableEndpoint(
        /**
         * @return The name of the table this endpoint corresponds to.
         */
        val name: String,
        /**
         * @return When placed in a top-level class, this is required to connect it to a provider.
         */
        val contentProvider: KClass<*>)
