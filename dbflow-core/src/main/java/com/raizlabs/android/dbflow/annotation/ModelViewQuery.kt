package com.raizlabs.android.dbflow.annotation

import com.raizlabs.android.dbflow.sql.Query

/**
 * Description: Represents a field that is a [Query]. This is only meant to be used as a query
 * reference in [ModelView]. This is so the annotation processor knows how to access the query of
 * the view.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelViewQuery
