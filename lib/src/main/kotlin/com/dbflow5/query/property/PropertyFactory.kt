package com.dbflow5.query.property

import com.dbflow5.config.FlowManager
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.NameAlias
import com.dbflow5.query.Operator
import kotlin.reflect.KClass

/**
 * Description: Provides some useful methods for creating [IProperty] from non-property types.
 */
object PropertyFactory {

    /**
     * Converts a char into a [Property] as its value represented by string.
     *
     * @param c the char to convert.
     * @return A new property.
     */
    @JvmStatic
    fun from(c: Char): Property<Char> {
        return Property(null, NameAlias.rawBuilder("'$c'")
            .build())
    }

    /**
     * Converts a int into a [Property] as its value represented by string.
     *
     * @param i the int to convert.
     * @return A new property.
     */
    @JvmStatic
    fun from(i: Int): Property<Int> {
        return Property(null, NameAlias.rawBuilder(i.toString() + "")
            .build())
    }

    /**
     * Converts a double into a [Property] as its value represented by string.
     *
     * @param d the double to convert.
     * @return A new property.
     */
    @JvmStatic
    fun from(d: Double): Property<Double> {
        return Property(null, NameAlias.rawBuilder(d.toString() + "")
            .build())
    }

    /**
     * Converts a long into a [Property] as its value represented by string.
     *
     * @param l the long to convert.
     * @return A new property.
     */
    @JvmStatic
    fun from(l: Long): Property<Long> {
        return Property(null, NameAlias.rawBuilder(l.toString() + "")
            .build())
    }

    /**
     * Converts a float into a [Property] as its value represented by string.
     *
     * @param f the float to convert.
     * @return A new property.
     */
    @JvmStatic
    fun from(f: Float): Property<Float> {
        return Property(null, NameAlias.rawBuilder(f.toString() + "")
            .build())
    }

    /**
     * Converts a short into a [Property] as its value represented by string.
     *
     * @param s the short to convert.
     * @return A new property.
     */
    @JvmStatic
    fun from(s: Short): Property<Short> {
        return Property(null, NameAlias.rawBuilder(s.toString() + "")
            .build())
    }

    /**
     * Converts a byte into a [Property] as its value represented by string.
     *
     * @param b the byte to convert.
     * @return A new property.
     */
    @JvmStatic
    fun from(b: Byte): Property<Byte> {
        return Property(null, NameAlias.rawBuilder(b.toString() + "")
            .build())
    }

    /**
     * Creates a new type-parameterized [Property] to be used as its value represented by a string
     * using [Operator.convertValueToString].
     *
     *
     * It will not convert a String column name
     * into a property, rather it assumes its database value represented by the String.
     *
     * @param type The object with value to use.
     * @param <T>  The parameter of its type.
     * @return A new property with its type.
     */
    @JvmStatic
    fun <T> from(type: T?): Property<T> {
        return Property(null, NameAlias.rawBuilder(
            Operator.convertValueToString(type) ?: "")
            .build())
    }


    /**
     * Creates a new type-parameterized [Property] from a [NameAlias] directly.
     *
     * @param nameAlias will not be copied. Rather used directly.
     * @return [NameAlias] as a [Property]
     */
    @JvmStatic
    fun from(nameAlias: NameAlias): Property<NameAlias> {
        return Property(null, nameAlias)
    }

    /**
     * Creates a new [Property] that is used to allow selects in a query.
     *
     * @param queriable The queriable to use and evaulated into a query.
     * @param [T] The model class of the query.
     * @return A new property that is a query.
     */
    @JvmStatic
    fun <T : Any> from(queriable: ModelQueriable<T>): Property<T> =
        from(queriable.table, "(${queriable.query.trim { it <= ' ' }})")

    /**
     * Creates a new type-parameterized [Property] to be used as its value represented by the string passed in.
     *
     * @param stringRepresentation The string representation of the object you wish to use.
     * @param [T]                 The parameter of its type.
     * @return A new property with its type.
     */
    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun <T> from(table: Class<T>, stringRepresentation: String?): Property<T> {
        return Property(null, NameAlias.rawBuilder(stringRepresentation ?: "")
            .build())
    }
}

val Int.property
    get() = PropertyFactory.from(this)

val Char.property
    get() = PropertyFactory.from(this)

val Double.property
    get() = PropertyFactory.from(this)

val Long.property
    get() = PropertyFactory.from(this)

val Float.property
    get() = PropertyFactory.from(this)

val Short.property
    get() = PropertyFactory.from(this)

val Byte.property
    get() = PropertyFactory.from(this)

val NameAlias.property
    get() = PropertyFactory.from(this)

val <T : Any> T?.property
    get() = PropertyFactory.from(this)

val <T : Any> ModelQueriable<T>.property
    get() = PropertyFactory.from(this)

inline fun <reified T : Any> propertyString(stringRepresentation: String?) = PropertyFactory.from(T::class.java, stringRepresentation)

inline fun <reified T : Any> KClass<T>.allProperty() = Property.allProperty(this.java)

/**
 * Convenience wrapper for creating a table name property used in queries.
 */
inline fun <reified T : Any> tableName() = propertyString<Any>(FlowManager.getTableName(T::class.java))

/**
 * For FTS tables, "docid" is allowed as an alias along with the usual "rowid", "oid" and "_oid_" identifiers.
 * Attempting to insert or update a row with a docid value that already exists in the table is
 * an error, just as it would be with an ordinary SQLite table.
 * There is one other subtle difference between "docid" and the normal SQLite aliases for the rowid column.
 * Normally, if an INSERT or UPDATE statement assigns discrete values to two or more aliases of the rowid column, SQLite writes the rightmost of such values specified in the INSERT or UPDATE statement to the database. However, assigning a non-NULL value to both the "docid" and one or more of the SQLite rowid aliases when inserting or updating an FTS table is considered an error. See below for an example.
 */
val docId: Property<Int> = propertyString("docid")
