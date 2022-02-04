package com.dbflow5.query.property

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.query.NameAlias
import kotlin.reflect.KClass

/**
 * Description: Provides convenience for types that are represented in different ways in the DB.
 *
 * @author Andrew Grosner (fuzz)
 */
class WrapperProperty<T, V> : Property<V> {

    private var databaseProperty: WrapperProperty<V, T>? = null

    override val adapter: SQLObjectAdapter<*>
        get() = super.adapter!!

    constructor(adapter: SQLObjectAdapter<*>, nameAlias: NameAlias) : super(adapter, nameAlias)

    constructor(adapter: SQLObjectAdapter<*>, columnName: String) : super(adapter, columnName)

    override fun withTable(): WrapperProperty<T, V> {
        val nameAlias = this.nameAlias
            .newBuilder()
            .withTable(adapter.name)
            .build()
        return WrapperProperty(this.adapter, nameAlias)
    }

    override fun withTable(tableNameAlias: NameAlias): WrapperProperty<T, V> {
        val nameAlias = this.nameAlias
            .newBuilder()
            .withTable(tableNameAlias.tableName)
            .build()
        return WrapperProperty(this.adapter, nameAlias)
    }

    /**
     * @return A new [Property] that corresponds to the inverted type of the [WrapperProperty]. Convenience
     * for types that have different DB representations.
     */
    fun invertProperty(): WrapperProperty<V, T> = databaseProperty
        ?: WrapperProperty<V, T>(adapter, nameAlias).also { databaseProperty = it }
}
