package com.dbflow5.query2

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.query.BaseOperator
import com.dbflow5.query.From
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.SQLOperator
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
import com.dbflow5.sql.Query

data class ColumnValue<T : IProperty<T>, V : Any>(
    val property: IProperty<T>? = null,
    val value: V?
)

private fun List<ColumnValue<*, *>>.toColumnValues(): Pair<List<IProperty<*>>, List<List<*>>> {
    val props = mapNotNull { it.property }
    val values = listOf(map { it.value })
    return props to values
}

internal fun <Table : Any> ModelAdapter<Table>.columnValue(
    sqlOperator: SQLOperator
): ColumnValue<out Property<*>, Any> {
    if (sqlOperator.columnName().isBlank()) {
        return ColumnValue(null, sqlOperator.value())
    }
    return ColumnValue(getProperty(sqlOperator.columnName()), sqlOperator.value())
}

interface HasValues<Table : Any> {
    /**
     * Overrides any specified columnValues by providing them here.
     * To specify equal number of column + values, use the supplied [insert] overloads.
     */
    fun values(vararg values: Any): InsertWithValues<Table>
}

interface HasSelect<Table : Any> {

    infix fun <TFrom : Any> select(from: From<TFrom>): InsertWithSelect<Table, TFrom>
}

/**
 * SQLite insert statement
 */
interface Insert<Table : Any> : Query,
    HasValues<Table>,
    HasSelect<Table>,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    Conflictable<InsertWithConflict<Table>>

interface InsertWithConflict<Table : Any> : Query,
    HasValues<Table>,
    HasSelect<Table>, HasConflictAction, Conflictable<InsertWithConflict<Table>>

interface InsertWithValues<Table : Any> : Query,
    HasValues<Table> {

    val columns: List<IProperty<*>>?
    val values: List<List<Any?>>
}

interface InsertWithSelect<Table : Any, TFrom : Any> : Query {

    val from: From<TFrom>?
}

fun <Table : Any> SQLObjectAdapter<Table>.insert(): Insert<Table> = InsertImpl(adapter = this)

fun <Table : Any> SQLObjectAdapter<Table>.insert(
    columnValue: ColumnValue<*, *>,
    vararg columnValues: ColumnValue<*, *>,
): InsertWithValues<Table> {
    val (columns, values) = mutableListOf(columnValue)
        .apply { addAll(columnValues.toList()) }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}

fun <Table : Any> ModelAdapter<Table>.insert(
    operator: SQLOperator,
    vararg operators: SQLOperator,
): InsertWithValues<Table> {
    val (columns, values) = mutableListOf(columnValue(operator))
        .apply { addAll(operators.map { columnValue(it) }) }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}

fun <Table : Any> ModelAdapter<Table>.insert(
    group: OperatorGroup,
): InsertWithValues<Table> {
    val (columns, values) = group.map { columnValue(it) }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}

/**
 * Same as calling [insert] with [columnValues] as all columns with '?' values.
 */
fun <Table : Any> ModelAdapter<Table>.insertAllAsTemplate(): Insert<Table> {
    val (columns, values) = allColumnProperties.map { ColumnValue(it, Property.WILDCARD) }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}


internal data class InsertImpl<Table : Any>(
    override val columns: List<IProperty<*>>? = null,
    override val values: List<List<Any?>> = listOf(),
    override val conflictAction: ConflictAction = ConflictAction.NONE,
    override val adapter: SQLObjectAdapter<Table>,
    override val from: From<Any>? = null,
) : InsertWithValues<Table>, InsertWithConflict<Table>, Insert<Table>,
    InsertWithSelect<Table, Any> {
    override val query: String by lazy {
        buildString {
            append("INSERT ")
            if (conflictAction != ConflictAction.NONE) {
                append("OR $conflictAction ")
            }
            append("INTO ${adapter.name}")
            columns
                ?.takeIf { it.isNotEmpty() }
                ?.let {
                    append(
                        "(${it.joinToString()})"
                    )
                }
            if (from != null) {
                append(" ${from.query}")
            } else {
                if (values.isEmpty()) {
                    throw IllegalStateException(
                        "The insert of ${adapter.name} " +
                            "should have at least one value specified for the insert"
                    )
                } else {
                    // guaranteed based on API we have same set of columns _and_ values
                    append(" VALUES(")
                    values.forEachIndexed { index, list ->
                        if (index > 0) append(",(")
                        append(BaseOperator.joinArguments(",", list))
                            .append(")")
                    }
                }
            }
        }
    }

    override fun or(action: ConflictAction): InsertWithConflict<Table> = copy(
        conflictAction = action,
    )

    /**
     * If [columnValues] already specified on creation, it will
     * joins the added [values] subsequently aligning to each set of columnValues.
     *
     * If the size of values do not equal existing map size, then this will
     * throw an [IllegalStateException].
     *
     * Subsequent calls will continue merging them.
     */
    override fun values(vararg values: Any): InsertWithValues<Table> {
        if (columns != null && columns.size != values.size) {
            throw IllegalStateException(
                """The Insert of ${adapter.name}
                |when specifying columns needs to have the same amount
                |of values and columns. found ${columns.size} != ${values.size}""".trimMargin()
            )
        }
        return copy(
            values = this.values.toMutableList().apply { add(values.toList()) },
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <TFrom : Any> select(from: From<TFrom>): InsertWithSelect<Table, TFrom> = copy(
        from = from as From<Any>,
    ) as InsertWithSelect<Table, TFrom>
}