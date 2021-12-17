package com.dbflow5.query

import com.dbflow5.byteArrayToHexString
import com.dbflow5.config.FlowManager
import com.dbflow5.converter.TypeConverter
import com.dbflow5.data.Blob
import com.dbflow5.query.property.Property
import com.dbflow5.sql.Query
import com.dbflow5.sqlEscapeString

/**
 * Description: Base class for all kinds of [SQLOperator]
 */
abstract class BaseOperator internal constructor(
    /**
     * The column name
     */
    protected var nameAlias: NameAlias?) : SQLOperator {

    /**
     * The operation such as "=", "&lt;", and more
     */
    protected var operation = ""

    /**
     * The value of the column we care about
     */
    protected var value: Any? = null

    /**
     * A custom SQL statement after the value of the Operator
     */
    protected var postArg: String? = null

    /**
     * An optional separator to use when chaining these together
     */
    protected var separator: String? = null

    /**
     * If true, the value is set and we should append it. (to prevent false positive nulls)
     */
    protected var isValueSet: Boolean = false

    /**
     * @return the value of the argument
     */
    override fun value(): Any? = value

    /**
     * @return the column name
     */
    override fun columnName(): String = nameAlias?.query ?: ""

    override fun separator(separator: String): SQLOperator {
        this.separator = separator
        return this
    }

    override fun separator(): String? = separator

    /**
     * @return true if has a separator defined for this condition.
     */
    override fun hasSeparator(): Boolean = separator?.isNotEmpty() ?: false

    /**
     * @return the operator such as "&lt;", "&gt;", or "="
     */
    override fun operation(): String = operation

    /**
     * @return An optional post argument for this condition
     */
    fun postArgument(): String? = postArg

    /**
     * @return internal alias used for subclasses.
     */
    internal fun columnAlias(): NameAlias? = nameAlias

    open fun convertObjectToString(obj: Any?, appendInnerParenthesis: Boolean): String =
        convertValueToString(obj, appendInnerParenthesis)

    companion object {

        @JvmStatic
        fun convertValueToString(value: Any?, appendInnerQueryParenthesis: Boolean): String =
            convertValueToString(value, appendInnerQueryParenthesis, true)

        /**
         * Converts a value input into a String representation of that.
         *
         *
         * If it has a [TypeConverter], it first will convert it's value into its [TypeConverter.getDBValue].
         *
         *
         * If the value is a [Number], we return a string rep of that.
         *
         *
         * If the value is a [BaseModelQueriable] and appendInnerQueryParenthesis is true,
         * we return the query wrapped in "()"
         *
         *
         * If the value is a [NameAlias], we return the [NameAlias.getQuery]
         *
         *
         * If the value is a [SQLOperator], we [SQLOperator.appendConditionToQuery].
         *
         *
         * If the value is a [Query], we simply call [Query.getQuery].
         *
         *
         * If the value if a [Blob] or byte[]
         *
         * @param value                       The value of the column in Model format.
         * @param appendInnerQueryParenthesis if its a [BaseModelQueriable] and an inner query value
         * in a condition, we append parenthesis to the query.
         * @return Returns the result as a string that's safe for SQLite.
         */
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun convertValueToString(value: Any?,
                                 appendInnerQueryParenthesis: Boolean,
                                 typeConvert: Boolean): String {
            if (value == null) {
                // Return to match NULL in SQLITE.
                return "NULL"
            } else {
                var locVal = value
                if (typeConvert) {
                    val typeConverter: TypeConverter<*, Any>? = FlowManager.getTypeConverterForClass(locVal.javaClass) as TypeConverter<*, Any>?
                    if (typeConverter != null) {
                        locVal = typeConverter.getDBValue(locVal)
                    }
                }
                return if (appendInnerQueryParenthesis && locVal is BaseModelQueriable<*>) {
                    locVal.enclosedQuery
                } else if (locVal is Property<*> && locVal == Property.WILDCARD) {
                    locVal.toString()
                } else when (locVal) {
                    is Number -> locVal.toString()
                    is Enum<*> -> sqlEscapeString(locVal.name)
                    is NameAlias -> locVal.query
                    is SQLOperator -> {
                        val queryBuilder = StringBuilder()
                        locVal.appendConditionToQuery(queryBuilder)

                        queryBuilder.toString()
                    }
                    is Query -> locVal.query
                    is Blob, is ByteArray -> {
                        val bytes: ByteArray? = if (locVal is Blob) {
                            locVal.blob
                        } else {
                            locVal as ByteArray?
                        }
                        "X${sqlEscapeString(byteArrayToHexString(bytes))}"
                    }
                    else -> sqlEscapeString(locVal.toString())
                }
            }
        }

        /**
         * Returns a string containing the tokens joined by delimiters and converted into the property
         * values for a query.
         *
         * @param delimiter The text to join the text with.
         * @param tokens    an [Iterable] of objects to be joined. Strings will be formed from
         * the objects by calling [.convertValueToString].
         * @return A joined string
         */
        @JvmStatic
        fun joinArguments(delimiter: CharSequence,
                          tokens: Iterable<*>,
                          condition: BaseOperator): String =
            tokens.joinToString(separator = delimiter) { condition.convertObjectToString(it, false) }

        /**
         * Returns a string containing the tokens converted into DBValues joined by delimiters.
         *
         * @param delimiter The text to join the text with.
         * @param tokens    an array objects to be joined. Strings will be formed from
         * the objects by calling object.toString().
         * @return A joined string
         */
        @JvmStatic
        fun joinArguments(delimiter: CharSequence, tokens: Array<Any?>): String =
            tokens.joinToString(separator = delimiter) {
                convertValueToString(it, false, true)
            }

        /**
         * Returns a string containing the tokens converted into DBValues joined by delimiters.
         *
         * @param delimiter The text to join the text with.
         * @param tokens    an array objects to be joined. Strings will be formed from
         * the objects by calling object.toString().
         * @return A joined string
         */
        @JvmStatic
        fun joinArguments(delimiter: CharSequence, tokens: Iterable<Any?>): String =
            tokens.joinToString(separator = delimiter) {
                convertValueToString(it, false, true)
            }
    }

}
