package com.dbflow5.query.property

import com.dbflow5.query.Join
import com.dbflow5.query.Method
import com.dbflow5.query.NameAlias
import com.dbflow5.query.OrderBy
import com.dbflow5.sql.Query
import com.dbflow5.structure.Model

/**
 * Description: Defines the base interface all property classes implement.
 */
interface IProperty<P : IProperty<P>> : Query {

    /**
     * @return The underlying [NameAlias] that represents the name of this property.
     */
    val nameAlias: NameAlias

    /**
     * @return The key used in placing values into cursor.
     */
    val cursorKey: String

    /**
     * @return the table this property belongs to.
     */
    val table: Class<*>?

    /**
     * @param aliasName The name of the alias.
     * @return A new [P] that expresses the current column name with the specified Alias name.
     */
    fun `as`(aliasName: String): P

    /**
     * Adds another property and returns as a new property. i.e p1 + p2
     *
     * @param property the property to add.
     * @return A new instance.
     */
    operator fun plus(property: IProperty<*>): P

    /**
     * Subtracts another property and returns as a new property. i.e p1 - p2
     *
     * @param property the property to subtract.
     * @return A new instance.
     */
    operator fun minus(property: IProperty<*>): P

    /**
     * Divides another property and returns as a new property. i.e p1 / p2
     *
     * @param property the property to divide.
     * @return A new instance.
     */
    operator fun div(property: IProperty<*>): P

    /**
     * Multiplies another property and returns as a new property. i.e p1 * p2
     *
     * @param property the property to multiply.
     * @return A new instance.
     */
    operator fun times(property: IProperty<*>): P

    /**
     * Modulous another property and returns as a new property. i.e p1 % p2
     *
     * @param property the property to calculate remainder of.
     * @return A new instance.
     */
    operator fun rem(property: IProperty<*>): P

    /**
     * Concats another property and returns as a new propert.y i.e. p1 || p2
     *
     * @param property The property to concatenate.
     * @return A new instance.
     */
    fun concatenate(property: IProperty<*>): P

    /**
     * @return Appends DISTINCT to the property name. This is handy in [Method] queries.
     * This distinct [P] can only be used with one column within a [Method].
     */
    fun distinct(): P

    /**
     * @return A property that represents the [Model] from which it belongs to. This is useful
     * in [Join] queries to represent this property.
     *
     *
     * The resulting [P] becomes `tableName`.`columnName`.
     */
    fun withTable(): P

    /**
     * @param tableNameAlias The name of the table to append. This may be different because of complex queries
     * that use a [NameAlias] for the table name.
     * @return A property that represents the [Model] from which it belongs to. This is useful
     * in [Join] queries to represent this property.
     *
     *
     * The resulting column name becomes `tableName`.`columnName`.
     */
    fun withTable(tableNameAlias: NameAlias): P

    fun asc(): OrderBy

    fun desc(): OrderBy
}
