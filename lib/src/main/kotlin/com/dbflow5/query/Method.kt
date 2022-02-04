package com.dbflow5.query


import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
import com.dbflow5.query.property.PropertyFactory
import com.dbflow5.query.property.propertyString
import com.dbflow5.query.property.tableName
import com.dbflow5.sql.SQLiteType

/**
 * Represents SQLite methods on columns. These act as [Property] so we can use them in complex
 * scenarios.
 */
open class Method(methodName: String, vararg properties: IProperty<*>) : Property<Any?>(null, "") {

    private val propertyList = arrayListOf<IProperty<*>>()
    private val operationsList = arrayListOf<String>()
    private val methodProperty: IProperty<*>
    private var _nameAlias: NameAlias? = null

    constructor(vararg properties: IProperty<*>) : this("", *properties)

    init {
        methodProperty = Property<Any>(null, NameAlias.rawBuilder(methodName).build())

        if (properties.isEmpty()) {
            propertyList.add(Property.ALL_PROPERTY)
        } else {
            properties.forEach { addProperty(it) }
        }
    }

    override fun plus(property: IProperty<*>): Method =
        append(property, " ${Operator.Operation.PLUS}")

    override fun minus(property: IProperty<*>): Method =
        append(property, " ${Operator.Operation.MINUS}")

    override fun div(property: IProperty<*>): Property<Any?> =
        append(property, " ${Operator.Operation.DIVISION}")

    override fun times(property: IProperty<*>): Property<Any?> =
        append(property, " ${Operator.Operation.MULTIPLY}")

    override fun rem(property: IProperty<*>): Property<Any?> =
        append(property, " ${Operator.Operation.MOD}")

    /**
     * Allows adding a property to the [Method]. Will remove the [Property.ALL_PROPERTY]
     * if it exists as first item.
     *
     * @param property The property to add.
     */
    fun addProperty(property: IProperty<*>): Method = append(property, ",")

    /**
     * Appends a property with the specified operation that separates it. The operation will appear before
     * the property specified.
     */
    fun append(property: IProperty<*>, operation: String): Method {
        // remove all property since its not needed when we specify a property.
        if (propertyList.size == 1 && propertyList[0] === Property.ALL_PROPERTY) {
            propertyList.removeAt(0)
        }
        propertyList.add(property)
        operationsList.add(operation)
        return this
    }

    protected fun getPropertyList(): List<IProperty<*>> = propertyList

    override val nameAlias: NameAlias
        get() {
            var alias = _nameAlias
            if (alias == null) {
                var query: String? = methodProperty.query
                if (query == null) {
                    query = ""
                }
                query += "("
                val propertyList = getPropertyList()
                for (i in propertyList.indices) {
                    val property = propertyList[i]
                    if (i > 0) {
                        query += "${operationsList[i]} "
                    }
                    query += property.toString()

                }
                query += ")"
                alias = NameAlias.rawBuilder(query).build()
            }
            this._nameAlias = alias
            return alias
        }

    /**
     * Represents the SQLite CAST operator.
     */
    class Cast internal constructor(private val property: IProperty<*>) {

        /**
         * @param sqLiteType The type of column to cast it to.
         * @return A new [Method] that represents the statement.
         */
        fun `as`(sqLiteType: SQLiteType): Property<Any?> {
            val newProperty = Property<Any?>(
                property.adapter,
                property.nameAlias
                    .newBuilder()
                    .shouldAddIdentifierToAliasName(false)
                    .`as`(sqLiteType.name)
                    .build()
            )
            return Method("CAST", newProperty)
        }

        /**
         * Returns a [Property] of [Int] so it can accept [Int] values.
         */
        @Suppress("UNCHECKED_CAST")
        fun asInteger(): Property<Int> = `as`(SQLiteType.INTEGER) as Property<Int>

        /**
         * Returns a [Property] of [Double] so it can accept [Double] values.
         */
        @Suppress("UNCHECKED_CAST")
        fun asReal(): Property<Double> = `as`(SQLiteType.REAL) as Property<Double>

        /**
         * Returns a [Property] of [String] so it can accept [String] values.
         */
        @Suppress("UNCHECKED_CAST")
        fun asText(): Property<String> = `as`(SQLiteType.TEXT) as Property<String>
    }

}

/**
 * @param properties Set of properties that the method acts on.
 * @return The average value of all properties within this group. The result is always a float from this statement
 * as long as there is at least one non-NULL input. The result may be NULL if there are no non-NULL columns.
 */
fun avg(vararg properties: IProperty<*>): Method = Method("AVG", *properties)

/**
 * @param properties Set of properties that the method acts on.
 * @return A count of the number of times that specified properties are not NULL in a group. Leaving
 * the properties empty returns COUNT(*), which is the total number of rows in the query.
 */
fun count(vararg properties: IProperty<*>): Method = Method("COUNT", *properties)

/**
 * @param properties Set of properties that the method acts on.
 * @return A string which is the concatenation of all non-NULL values of the properties.
 */
fun groupConcat(vararg properties: IProperty<*>): Method =
    Method("GROUP_CONCAT", *properties)

/**
 * @param properties Set of properties that the method acts on.
 * @return The method that represents the max of the specified columns/properties.
 */
fun max(vararg properties: IProperty<*>): Method = Method("MAX", *properties)

/**
 * @param properties Set of properties that the method acts on.
 * @return The method that represents the min of the specified columns/properties.
 */
fun min(vararg properties: IProperty<*>): Method = Method("MIN", *properties)

/**
 * @param properties Set of properties that the method acts on.
 * @return The method that represents the sum of the specified columns/properties.
 */
fun sum(vararg properties: IProperty<*>): Method = Method("SUM", *properties)

/**
 * @param properties Set of properties that the method acts on.
 * @return The method that represents the total of the specified columns/properties.
 */
fun total(vararg properties: IProperty<*>): Method = Method("TOTAL", *properties)

/**
 * @param property The property to cast.
 * @return A new CAST object. To complete use the [Cast. as] method.
 */
fun cast(property: IProperty<*>): Method.Cast = Method.Cast(property)

fun replace(property: IProperty<*>, findString: String, replacement: String): Method =
    Method(
        "REPLACE",
        property,
        PropertyFactory.from<Any>(findString),
        PropertyFactory.from<Any>(replacement)
    )

/**
 * SQLite standard "strftime()" method. See SQLite documentation on this method.
 */
fun strftime(
    formatString: String,
    timeString: String, vararg modifiers: String
): Method {
    val propertyList = arrayListOf<IProperty<*>>()
    propertyList.add(PropertyFactory.from<Any>(formatString))
    propertyList.add(PropertyFactory.from<Any>(timeString))
    modifiers.mapTo(propertyList) { PropertyFactory.from<Any>(it) }
    return Method("strftime", *propertyList.toTypedArray())
}

/**
 * Sqlite "datetime" method. See SQLite documentation on this method.
 */
fun datetime(timeStamp: Long, vararg modifiers: String): Method {
    val propertyList = arrayListOf<IProperty<*>>()
    propertyList.add(PropertyFactory.from(timeStamp))
    modifiers.mapTo(propertyList) { PropertyFactory.from<Any>(it) }
    return Method("datetime", *propertyList.toTypedArray())
}

/**
 * Sqlite "date" method. See SQLite documentation on this method.
 */
fun date(
    timeString: String,
    vararg modifiers: String
): Method {
    val propertyList = arrayListOf<IProperty<*>>()
    propertyList.add(PropertyFactory.from<Any>(timeString))
    modifiers.mapTo(propertyList) { PropertyFactory.from<Any>(it) }
    return Method("date", *propertyList.toTypedArray())
}

/**
 * @return Constructs using the "IFNULL" method in SQLite. It will pick the first non null
 * value and set that. If both are NULL then it will use NULL.
 */
fun ifNull(
    first: IProperty<*>,
    secondIfFirstNull: IProperty<*>
): Method = Method("IFNULL", first, secondIfFirstNull)

/**
 * @return Constructs using the "NULLIF" method in SQLite. If both expressions are equal, then
 * NULL is set into the DB.
 */
fun nullIf(
    first: IProperty<*>,
    second: IProperty<*>
): Method = Method("NULLIF", first, second)

@JvmField
val random: Method = Method("RANDOM", Property.NO_PROPERTY)

/**
 * Used for FTS:
 *
 * For a SELECT query that uses the full-text index, the offsets() function returns a
 * text value containing a series of space-separated integers. For each term in each phrase
 * match of the current row, there are four integers in the returned list.
 * Each set of four integers is interpreted as follows:
 * Integer	Interpretation
 *  0	The column number that the term instance occurs in (0 for the leftmost column of the FTS table, 1 for the next leftmost, etc.).
 *  1	The term number of the matching term within the full-text query expression. Terms within a query expression are numbered starting from 0 in the order that they occur.
 *  2	The byte offset of the matching term within the column.
 *  3	The size of the matching term in bytes.
 *
 *  For more see sqlite.org
 */
inline fun <reified T : Any> offsets() = Method("offsets", tableName<T>())

/**
 * Used for FTS:
 * The snippet function is used to create formatted fragments of document text for
 * display as part of a full-text query results report.
 *
 * @param start - the start match text.
 * @param end - the end match text
 * @param ellipses
 * @param index - The FTS table column number to extract the returned fragments of
 * text from. Columns are numbered from left to right starting with zero.
 * A negative value indicates that the text may be extracted from any column.
 * @param approximateTokens - The absolute value of this integer argument is used as the
 * (approximate) number of tokens to include in the returned text value.
 * The maximum allowable absolute value is 64.
 *
 * For more see sqlite.org
 */
inline fun <reified T : Any> snippet(
    start: String? = null,
    end: String? = null,
    ellipses: String? = null,
    index: Int? = null,
    approximateTokens: Int? = null,
): Method {
    val args = listOfNotNull(tableName<T>(), start, end, ellipses, index, approximateTokens)
        .map {
            if (it is String) propertyString("'${it}'")
            else propertyString<Any>(it.toString())
        }.toTypedArray()
    return Method("snippet", *args)
}
