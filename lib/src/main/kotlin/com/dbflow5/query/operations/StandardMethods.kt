package com.dbflow5.query.operations

import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.data.Blob
import com.dbflow5.sql.SQLiteType

interface StandardMethod {
    val name: String
}

data class AllParametersMethod<ReturnType>(
    override val name: String,
) : StandardMethod {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(
        vararg properties: AnyOperator
    ): Method<ReturnType> =
        method(
            name = name,
            valueConverter = InferredObjectConverter as SQLValueConverter<ReturnType>,
            arguments = properties,
        )
}


data class SingleParametersMethod<ReturnType>(
    override val name: String,
) : StandardMethod {
    inline operator fun <reified ReturnType> invoke(
        property: AnyProperty,
    ): Method<ReturnType> =
        method(name, property)
}

/**
 * The average value of all properties within this group. The result is always a float from this statement
 * as long as there is at least one non-NULL input. The result may be NULL if there are no non-NULL columns.
 */
val avg = AllParametersMethod<Double>("AVG")

fun cast(property: PropertyStart<*, *>): Cast = Cast(property)

class Cast(val property: PropertyStart<*, *>) : StandardMethod {
    override val name: String = "CAST"

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
val count = AllParametersMethod<Long>("COUNT")

/**
 * A string which is the concatenation of all non-NULL values of the properties.
 */
val groupConcat = AllParametersMethod<String>("GROUP_CONCAT")

/**
 * The method that represents the max of the specified columns/properties.
 *
 * This has type parameters since MAX can represent any supported data type.
 */
fun <ReturnType> max() = AllParametersMethod<ReturnType>("MAX")

/**
 * The method that represents the min of the specified columns/properties.
 *
 * This has type parameters since MIN can represent any supported data type.
 */
fun <ReturnType> min() = AllParametersMethod<ReturnType>("MIN")

/**
 * The method that represents the sum of the specified columns/properties.
 */
val sum = AllParametersMethod<Long>("SUM")

/**
 * The method that represents the total of the specified columns/properties. Same as [sum],
 * except it returns 0.0 when all inputs are NULL.
 */
val total = AllParametersMethod<Double>("TOTAL")

val replace = Replace

object Replace : StandardMethod {
    override val name: String = "REPLACE"

    operator fun invoke(
        property: AnyProperty,
        findString: String,
        replacement: String
    ): Method<String> = method(
        name, property,
        sqlLiteralOf(findString),
        sqlLiteralOf(replacement),
    )
}

/**
 * SQLite standard "strftime()" method.
 * See SQLite documentation on this method.
 */
val strftime = Strftime

object Strftime : StandardMethod {
    override val name: String = "strftime"

    operator fun invoke(
        formatString: String,
        timeString: String,
        vararg modifiers: String
    ): Method<String> = method(
        name,
        sqlLiteralOf(formatString),
        sqlLiteralOf(timeString),
        *modifiers.map { sqlLiteralOf(it) }.toTypedArray(),
    )
}

/**
 * Sqlite "datetime" method. See SQLite documentation on this method.
 */
val datetime = DateTime

object DateTime : StandardMethod {
    override val name: String = "datetime"

    operator fun invoke(
        timeStamp: Long,
        vararg modifiers: String,
    ): Method<String> = method(
        name,
        sqlLiteralOf(timeStamp),
        *modifiers.map { sqlLiteralOf(it) }
            .toTypedArray(),
    )
}

/**
 * Sqlite "date" method. See SQLite documentation on this method.
 */
val date = Date

object Date : StandardMethod {
    override val name: String = "date"

    operator fun invoke(
        timeString: String,
        vararg modifiers: String,
    ): Method<String> = method(
        name,
        sqlLiteralOf(timeString),
        *modifiers.map { sqlLiteralOf(it) }
            .toTypedArray(),
    )
}

/**
 * Constructs using the "IFNULL" method in SQLite. It will pick the first non null
 * value and set that. If both are NULL then it will use NULL.
 */
val ifNull = IfNull

object IfNull : StandardMethod {
    override val name: String = "IFNULL"

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
val nullIf = NullIf

object NullIf : StandardMethod {
    override val name: String = "NULLIF"

    operator fun invoke(
        first: AnyOperator,
        second: AnyOperator
    ): Method<Any?> = method(
        name,
        first,
        second,
    )
}

val random = Random()

object Random : StandardMethod {
    override val name: String = "RANDOM"

    operator fun invoke(): Method<Unit> = emptyMethod(
        name,
    )
}

val offsets = Offsets

object Offsets : StandardMethod {
    override val name: String = "offsets"

    inline operator fun <reified Table : Any> invoke(): Method<String> =
        method(
            name,
            makeLazySQLObjectAdapter(Table::class).tableNameLiteral(),
        )
}

val snippet = Snippet

object Snippet : StandardMethod {
    override val name: String = "snippet"

    inline operator fun <reified Table : Any> invoke(
        start: String? = null,
        end: String? = null,
        ellipses: String? = null,
        index: Int? = null,
        approximateTokens: Int? = null,
    ): Method<String> {
        val args = listOfNotNull(start, end, ellipses, index, approximateTokens).map {
            sqlLiteralOf(it)
        }
        return method(
            name,
            makeLazySQLObjectAdapter(Table::class).tableNameLiteral(),
            *args.toTypedArray(),
        )
    }
}

