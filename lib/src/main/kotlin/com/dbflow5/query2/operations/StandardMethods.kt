package com.dbflow5.query2.operations

import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.data.Blob
import com.dbflow5.sql.SQLiteType

interface StandardMethod {
    val name: String
}

interface AllParametersMethod<ReturnType> : StandardMethod
interface SingleParametersMethod<ReturnType> : StandardMethod

inline operator fun <reified ReturnType> AllParametersMethod<ReturnType>.invoke(
    vararg properties: AnyOperator
): Method<ReturnType> =
    method(name, *properties)

inline operator fun <reified ReturnType> SingleParametersMethod<ReturnType>.invoke(
    property: AnyProperty,
): Method<ReturnType> =
    method(name, property)

sealed class StandardMethods(val name: String) {

    /**
     * The average value of all properties within this group. The result is always a float from this statement
     * as long as there is at least one non-NULL input. The result may be NULL if there are no non-NULL columns.
     */
    object Avg : StandardMethods("AVG"), AllParametersMethod<Double>

    class Cast(val property: PropertyStart<*, *>) : StandardMethods("CAST") {
        // TODO: use reified inference for return type?
        inline infix fun <reified ReturnType> `as`(sqLiteType: SQLiteType): Method<ReturnType> =
            method(
                name, property.`as`(
                    sqLiteType.name,
                    shouldAddIdentifierToAlias = false
                )
            )

        fun asInteger(): Method<Int> = `as`(SQLiteType.INTEGER)

        fun asReal(): Method<Double> = `as`(SQLiteType.REAL)

        fun asText(): Method<String> = `as`(SQLiteType.TEXT)

        fun asBlob(): Method<Blob> = `as`(SQLiteType.BLOB)
    }

    /**
     * A count of the number of times that specified properties are not NULL in a group. Leaving
     * the properties empty returns COUNT(*), which is the total number of rows in the query.
     */
    object Count : StandardMethods("COUNT"), AllParametersMethod<Long>

    /**
     * A string which is the concatenation of all non-NULL values of the properties.
     */
    object GroupConcat : StandardMethods("GROUP_CONCAT"), AllParametersMethod<String>

    /**
     * The method that represents the max of the specified columns/properties.
     *
     * This has type parameters since MAX can represent any supported data type.
     */
    class Max<ReturnType> : StandardMethods("MAX"), AllParametersMethod<ReturnType>

    /**
     * The method that represents the min of the specified columns/properties.
     *
     * This has type parameters since MIN can represent any supported data type.
     */
    class Min<ReturnType> : StandardMethods("MIN"), AllParametersMethod<ReturnType>

    /**
     * The method that represents the sum of the specified columns/properties.
     */
    object Sum : StandardMethods("SUM"), AllParametersMethod<Long>

    /**
     * The method that represents the total of the specified columns/properties. Same as [Sum],
     * except it returns 0.0 when all inputs are NULL.
     */
    object Total : StandardMethods("TOTAL"), AllParametersMethod<Double>

    // TODO: cast

    object Replace : StandardMethods("REPLACE") {
        operator fun invoke(
            property: AnyProperty,
            findString: String,
            replacement: String
        ): Method<String> = method(
            name, property,
            literalOf(findString),
            literalOf(replacement),
        )
    }

    /**
     * SQLite standard "strftime()" method.
     * See SQLite documentation on this method.
     */
    object StrfTime : StandardMethods("strftime") {
        operator fun invoke(
            formatString: String,
            timeString: String,
            vararg modifiers: String
        ): Method<String> = method(
            name,
            literalOf(formatString),
            literalOf(timeString),
            *modifiers.map { literalOf(it) }.toTypedArray(),
        )
    }

    /**
     * Sqlite "datetime" method. See SQLite documentation on this method.
     */
    object DateTime : StandardMethods("datetime") {
        operator fun invoke(
            timeStamp: Long,
            vararg modifiers: String,
        ): Method<String> = method(
            name,
            literalOf(timeStamp),
            *modifiers.map { literalOf(it) }.toTypedArray(),
        )
    }

    /**
     * Sqlite "date" method. See SQLite documentation on this method.
     */
    object Date : StandardMethods("date") {
        operator fun invoke(
            timeString: String,
            vararg modifiers: String,
        ): Method<String> = method(
            name,
            literalOf(timeString),
            *modifiers.map { literalOf(it) }.toTypedArray(),
        )
    }

    /**
     * Constructs using the "IFNULL" method in SQLite. It will pick the first non null
     * value and set that. If both are NULL then it will use NULL.
     */
    object IfNull : StandardMethods("IFNULL") {
        operator fun invoke(
            first: AnyOperator,
            secondIfFirstNull: AnyOperator,
        ): Method<Any?> = method(
            name,
            first,
            secondIfFirstNull,
        )
    }

    /**
     * Constructs using the "NULLIF" method in SQLite. If both expressions are equal, then
     * NULL is set into the DB.
     */
    object NullIf : StandardMethods("NULLIF") {
        operator fun invoke(
            first: AnyOperator,
            second: AnyOperator
        ): Method<Any?> = method(
            name,
            first,
            second,
        )
    }

    object Random : StandardMethods("RANDOM") {
        operator fun invoke(): Method<Unit> = emptyMethod(
            name,
        )
    }

    object Offsets : StandardMethods("offsets") {
        inline operator fun <reified Table : Any> invoke(): Method<String> =
            method(
                name,
                makeLazySQLObjectAdapter(Table::class).tableNameLiteral(),
            )
    }

    object Snippet : StandardMethods("snippet") {
        inline operator fun <reified Table : Any> invoke(
            start: String? = null,
            end: String? = null,
            ellipses: String? = null,
            index: Int? = null,
            approximateTokens: Int? = null,
        ): Method<String> {
            val args = listOfNotNull(start, end, ellipses, index, approximateTokens).map {
                literalOf(it)
            }
            return method(
                name,
                makeLazySQLObjectAdapter(Table::class).tableNameLiteral(),
                *args.toTypedArray(),
            )
        }
    }

}

