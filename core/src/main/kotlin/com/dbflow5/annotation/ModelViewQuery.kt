package com.dbflow5.annotation

import com.dbflow5.sql.Query

/**
 * Description: Represents a field that is a [Query]. This is only meant to be used as a query
 * reference in [ModelView]. This is so the annotation processor knows how to access the query of
 * the view.
 */
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelViewQuery
