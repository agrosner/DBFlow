package com.raizlabs.android.dbflow.sql.language.property

import com.raizlabs.android.dbflow.sql.language.NameAlias
import com.raizlabs.android.dbflow.sql.language.Operator
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable

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
                Operator.convertValueToString(type))
                .build())
    }

    /**
     * Creates a new [Property] that is used to allow selects in a query.
     *
     * @param queriable The queriable to use and evaulated into a query.
     * @param <TModel>  The model class of the query.
     * @return A new property that is a query.
     */
    @JvmStatic
    fun <TModel> from(queriable: ModelQueriable<TModel>): Property<TModel> {
        return from(queriable.table, "(${queriable.query.trim { it <= ' ' }})")
    }

    /**
     * Creates a new type-parameterized [Property] to be used as its value represented by the string passed in.
     *
     * @param stringRepresentation The string representation of the object you wish to use.
     * @param <T>                  The parameter of its type.
     * @return A new property with its type.
     */
    @JvmStatic
    fun <T> from(table: Class<T>, stringRepresentation: String?): Property<T> {
        return Property(null, NameAlias.rawBuilder(stringRepresentation)
                .build())
    }
}
