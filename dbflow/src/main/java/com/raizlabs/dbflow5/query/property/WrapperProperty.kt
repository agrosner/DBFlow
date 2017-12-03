package com.raizlabs.dbflow5.query.property

import com.raizlabs.dbflow5.query.NameAlias

/**
 * Description: Provides convenience for types that are represented in different ways in the DB.
 *
 * @author Andrew Grosner (fuzz)
 */
class WrapperProperty<T, V> : Property<V> {

    private var databaseProperty: WrapperProperty<V, T>? = null

    constructor(table: Class<*>, nameAlias: NameAlias) : super(table, nameAlias)

    constructor(table: Class<*>, columnName: String) : super(table, columnName)

    /**
     * @return A new [Property] that corresponds to the inverted type of the [WrapperProperty]. Convenience
     * for types that have different DB representations.
     */
    fun invertProperty(): Property<T> {
        if (databaseProperty == null) {
            databaseProperty = WrapperProperty(getTable()!!, nameAlias)
        }
        return databaseProperty!!
    }
}
