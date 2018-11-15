package com.dbflow5.annotation

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
        val allFields: Boolean = true,

        /**
         * @return If true, we throw away checks for column indexing and simply assume that the cursor returns
         * all our columns in order. This may provide a slight performance boost.
         */
        val orderedCursorLookUp: Boolean = false,
        /**
         * @return When true, we reassign the corresponding Model's fields to default values when loading
         * from cursor. If false, we assign values only if present in Cursor.
         */
        val assignDefaultValuesFromCursor: Boolean = true)
