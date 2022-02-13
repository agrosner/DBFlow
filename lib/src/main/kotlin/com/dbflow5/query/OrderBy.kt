package com.dbflow5.query

import com.dbflow5.annotation.Collate
import com.dbflow5.query.property.IProperty
import com.dbflow5.query2.operations.Property
import com.dbflow5.query2.operations.StandardMethods
import com.dbflow5.sql.Query

/**
 * Description: Class that represents a SQL order-by.
 */
class OrderBy
@JvmOverloads
constructor(
    private val column: NameAlias? = null,
    /**
     * If true, append ASC, if false append DESC, or if null, then don't append.
     */
    private var isAscending: Boolean? = true
) : Query {

    private var collation: Collate? = null
    private var orderByString: String?

    override val query: String
        get() {
            val locOrderByString = orderByString
            return if (locOrderByString == null) {
                val query = StringBuilder()
                    .append(column)
                    .append(" ")
                if (collation != null) {
                    query.append("COLLATE $collation ")
                }
                isAscending?.let { isAscending ->
                    query.append(if (isAscending) ASCENDING else DESCENDING)
                }
                query.toString()
            } else {
                locOrderByString
            }
        }

    internal constructor(property: IProperty<*>) : this(property.nameAlias)

    internal constructor(orderByString: String) : this(column = null) {
        this.orderByString = orderByString
    }

    init {
        this.orderByString = null
    }

    fun ascending() = apply {
        isAscending = true
    }

    fun descending() = apply {
        isAscending = false
    }

    infix fun collate(collate: Collate) = apply {
        this.collation = collate
    }

    override fun toString(): String = query

    companion object {

        const val ASCENDING = "ASC"

        const val DESCENDING = "DESC"

        @JvmStatic
        fun fromProperty(property: Property<*, *>, isAscending: Boolean = true): OrderBy =
            OrderBy(
                property.nameAlias,
                // if we use RANDOM(), leave out ascending qualifier as its not valid SQLite.
                if (property == StandardMethods.Random()) {
                    null
                } else isAscending
            )

        @JvmStatic
        fun fromProperty(
            property: com.dbflow5.query.property.Property<*>,
            isAscending: Boolean = true
        ): OrderBy =
            OrderBy(
                property.nameAlias,
                // if we use RANDOM(), leave out ascending qualifier as its not valid SQLite.
                if (property == random) {
                    null
                } else isAscending
            )

        @JvmStatic
        fun fromNameAlias(nameAlias: NameAlias, isAscending: Boolean = true): OrderBy =
            OrderBy(nameAlias, isAscending)

        /**
         * Starts an [OrderBy] with RANDOM() query.
         */
        fun random(): OrderBy = OrderBy(random.nameAlias, isAscending = null)

        @JvmStatic
        fun fromString(orderByString: String): OrderBy = OrderBy(orderByString)

    }
}