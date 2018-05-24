package com.raizlabs.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description: Marks a Model class as NOT a [Table], but generates code for retrieving data from a
 * generic query
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class QueryModel(
        /**
         * @return Specify the class of the database to use.
         */
        val database: KClass<*>,
        /**
         * @return If true, all accessible, non-static, and non-final fields are treated as valid fields.
         * @see Table.allFields
         */
        val allFields: Boolean = true)
