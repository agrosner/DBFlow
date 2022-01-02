package com.dbflow5.annotation

import kotlin.reflect.KClass

@Deprecated(
    "Replaced with Query",
    replaceWith = ReplaceWith("com.dbflow5.annotation.Query")
)
typealias QueryModel = Query

/**
 * Description: Marks a Model class as NOT a [Table], but generates code for retrieving data from a
 * generic query
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Query(
    /**
     * @return Specify the database class that this table belongs to.
     * It must have the [Database] annotation. If left blank, the corresponding [Database]
     * should include it.
     */
    val database: KClass<*> = Any::class,
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
    val assignDefaultValuesFromCursor: Boolean = true
)
