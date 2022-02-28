package com.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description: Code generates a parent-child relation using [Query].
 * This by default grabs the [PrimaryKey] of the defining table and [ForeignKey] on child table.
 * The child table must have a [ForeignKey] on the defining table.
 */
annotation class OneToMany(
    /**
     * The referenced child table.
     */
    val childTable: KClass<*>,

    /**
     * By default conjoins the names of the two class references.
     */
    val generatedClassName: String = "",

    /**
     * The name in query to use. By default is lowercase variant of table class name.
     */
    val parentFieldName: String = "",

    /**
     * The name of child list to use. By default is "children".
     */
    val childListFieldName: String = "children",
)
