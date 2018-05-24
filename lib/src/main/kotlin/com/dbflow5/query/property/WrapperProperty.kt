package com.dbflow5.query.property

import com.dbflow5.config.FlowManager
import com.dbflow5.query.NameAlias

/**
 * Description: Provides convenience for types that are represented in different ways in the DB.
 *
 * @author Andrew Grosner (fuzz)
 */
class WrapperProperty<T, V> : Property<V> {

    private var databaseProperty: WrapperProperty<V, T>? = null

    override val table: Class<*>
        get() = super.table!!

    constructor(table: Class<*>, nameAlias: NameAlias) : super(table, nameAlias)

    constructor(table: Class<*>, columnName: String) : super(table, columnName)

    override fun withTable(): WrapperProperty<T, V> {
        val nameAlias = this.nameAlias
                .newBuilder()
                .withTable(FlowManager.getTableName(table))
                .build()
        return WrapperProperty(this.table, nameAlias)
    }

    override fun withTable(tableNameAlias: NameAlias): WrapperProperty<T, V> {
        val nameAlias = this.nameAlias
                .newBuilder()
                .withTable(tableNameAlias.tableName)
                .build()
        return WrapperProperty(this.table, nameAlias)
    }

    /**
     * @return A new [Property] that corresponds to the inverted type of the [WrapperProperty]. Convenience
     * for types that have different DB representations.
     */
    fun invertProperty(): WrapperProperty<V, T> = databaseProperty
            ?: WrapperProperty<V, T>(table, nameAlias).also { databaseProperty = it }
}
