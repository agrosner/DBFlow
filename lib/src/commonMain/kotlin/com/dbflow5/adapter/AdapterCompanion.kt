package com.dbflow5.adapter

import kotlin.reflect.KClass

/**
 * Marker implemented by `@Table`, `@Query`, and `@ModelView` companions so
 * `select from User` and companion column properties resolve without a `*_Table` type.
 */
interface AdapterCompanion<Model : Any> {

    val table: KClass<Model>
}
