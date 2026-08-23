package com.dbflow5.adapter

import kotlin.reflect.KClass

/**
 * Implemented by generated `@Table`, `@Query`, and `@ModelView` companions.
 *
 * Each companion is also the runtime adapter (`ModelAdapter`, `ViewAdapter`, or
 * `QueryAdapter`) via [ModelAdapterImpl], [ViewAdapterImpl], or [QueryAdapterImpl].
 * That lets you use `User.name` column properties, `select from User`, and
 * `userAdapter` interchangeably without a separate `*_Table` type or delegate object.
 */
interface AdapterCompanion<Model : Any> {

    val table: KClass<Model>
}
