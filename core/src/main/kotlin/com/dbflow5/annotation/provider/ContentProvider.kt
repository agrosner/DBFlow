package com.dbflow5.annotation.provider

import kotlin.reflect.KClass

/**
 * Description: Defines a Content Provider that gets generated.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class ContentProvider(
        /**
         * @return The authority URI for this provider.
         */
        val authority: String,
        /**
         * @return The class of the database this belongs to
         */
        val database: KClass<*>,
        /**
         * @return The base content uri String to use for all paths
         */
        val baseContentUri: String = "")
