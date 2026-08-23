package com.dbflow5.query

import com.dbflow5.annotation.Collate
import com.dbflow5.query.operations.Property
import com.dbflow5.query.methods.random
import com.dbflow5.sql.Query
import kotlin.jvm.JvmOverloads

interface HasOrderDirection {
    fun asc(): OrderByDirectional = direction(true)
    fun desc(): OrderByDirectional = direction(false)
    fun direction(ascending: Boolean): OrderByDirectional
}

interface OrderBy : Query

interface OrderByStart : OrderBy,
    HasOrderDirection {
    infix fun collate(collate: Collate): OrderByCollate
}

interface OrderByCollate : OrderBy,
    HasOrderDirection

interface OrderByDirectional :
    OrderBy

fun orderBy(
    nameAlias: NameAlias,
): OrderByStart = OrderByImpl(
    nameAlias = nameAlias,
)

fun orderBy(
    property: Property<*, *>,
) = orderBy(property.nameAlias)

/**
 * Starts an [OrderBy] with RANDOM() query.
 */
fun orderByRandom(): OrderByStart = OrderByImpl(
    nameAlias = random.nameAlias,
)

/**
 * Description: Class that represents a SQL order-by.
 */
internal data class OrderByImpl
@JvmOverloads
constructor(
    private val nameAlias: NameAlias,
    /**
     * If true, append ASC, if false append DESC, or if null, then don't append.
     */
    private val isAscending: Boolean? = true,
    private val collation: Collate = Collate.None,
) : OrderBy,
    OrderByStart,
    OrderByDirectional,
    OrderByCollate {

    override val query: String by lazy {
        buildString {
            append(nameAlias.query)
            if (collation != Collate.None) append(" ${collation.query}")
            if (isAscending != null) {
                append(" ${if (isAscending) "ASC" else "DESC"}")
            }
        }
    }

    override fun direction(ascending: Boolean): OrderByDirectional =
        copy(
            isAscending = ascending,
        )

    override fun collate(collate: Collate): OrderByCollate =
        copy(
            collation = collate,
        )
}

