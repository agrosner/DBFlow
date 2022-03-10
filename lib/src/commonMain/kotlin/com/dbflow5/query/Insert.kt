@file:Suppress("UNCHECKED_CAST")

package com.dbflow5.query

import com.dbflow5.adapter.WritableDBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.operations.BaseOperator
import com.dbflow5.query.operations.InferredObjectConverter
import com.dbflow5.query.operations.Property
import com.dbflow5.sql.Query

data class ColumnValue<T : Any, V>(
    val property: Property<V, T>? = null,
    val value: Any
)

private fun <Table : Any> List<ColumnValue<Table, Any>>.toColumnValues(): Pair<List<Property<*, Table>>, List<List<Any>>> {
    val props = mapNotNull { it.property }
    val values = listOf(map { it.value })
    return props to values
}

internal fun <Table : Any> WritableDBRepresentable<Table>.columnValue(
    sqlOperator: BaseOperator.SingleValueOperator<Any>
): ColumnValue<Table, Any> {
    if (sqlOperator.nameAlias.query.isBlank()) {
        return ColumnValue(null, sqlOperator.value)
    }
    return ColumnValue(
        getProperty(sqlOperator.nameAlias.query) as Property<Any, Table>,
        sqlOperator.value
    )
}

interface HasValues<Table : Any> {
    /**
     * Overrides any specified columnValues by providing them here.
     * To specify equal number of column + values, use the supplied [insert] overloads.
     */
    fun values(vararg values: Any): InsertWithValues<Table>
}

interface HasSelect<Table : Any> {

    infix fun <OtherTable : Any> select(
        subQuery: ExecutableQuery<SelectResult<OtherTable>>
    ): InsertWithSelect<Table, OtherTable>
}

interface Insert<Table : Any> : Query,
    ExecutableQuery<Long>

/**
 * SQLite insert statement
 */
interface InsertStart<Table : Any> :
    Insert<Table>,
    HasValues<Table>,
    HasSelect<Table>,
    HasAdapter<Table, WritableDBRepresentable<Table>>,
    Conflictable<InsertWithConflict<Table>>

interface InsertWithConflict<Table : Any> :
    Insert<Table>,
    HasValues<Table>,
    HasSelect<Table>, HasConflictAction, Conflictable<InsertWithConflict<Table>>

interface InsertWithValues<Table : Any> :
    Insert<Table>,
    HasValues<Table> {

    val columns: List<Property<*, Table>>?
    val values: List<List<Any?>>
}

interface InsertWithSelect<Table : Any, TFrom : Any> :
    Insert<Table> {
    val subquery: ExecutableQuery<SelectResult<TFrom>>?
}

fun <Table : Any> WritableDBRepresentable<Table>.insert(): InsertStart<Table> = InsertImpl(adapter = this)

fun <Table : Any> WritableDBRepresentable<Table>.insert(
    columnValue: ColumnValue<Table, *>,
    vararg columnValues: ColumnValue<Table, *>,
): InsertWithValues<Table> {
    val (columns, values) = mutableListOf(columnValue)
        .run {
            addAll(columnValues.toList())
            this as List<ColumnValue<Table, Any>>
        }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}

fun <Table : Any> WritableDBRepresentable<Table>.insert(
    columnValuePair: Pair<Property<*, Table>?, *>,
    vararg columnValues: Pair<Property<*, Table>?, *>,
): InsertWithValues<Table> {
    val (columns, values) = mutableListOf(
        ColumnValue(
            columnValuePair.first as Property<Any, Table>,
            columnValuePair.second as Any
        )
    )
        .apply {
            addAll(columnValues
                .map {
                    ColumnValue(
                        it.first as Property<Any, Table>,
                        it.second as Any
                    )
                }
                .toList())
        }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}

fun <Table : Any> WritableDBRepresentable<Table>.insert(
    operator: BaseOperator.SingleValueOperator<*>,
    vararg operators: BaseOperator.SingleValueOperator<*>,
): InsertWithValues<Table> {
    val (columns, values) = mutableListOf(columnValue(operator as BaseOperator.SingleValueOperator<Any>))
        .apply { addAll(operators.map { columnValue(it as BaseOperator.SingleValueOperator<Any>) }) }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}

/**
 * Insert statement with property column names. Used
 * for [InsertWithSelect].
 */
fun <Table : Any> ModelAdapter<Table>.insert(
    property: Property<*, Table>,
    vararg properties: Property<*, Table>,
): InsertWithConflict<Table> {
    return InsertImpl(
        columns = mutableListOf(property).apply { addAll(properties) },
        adapter = this,
    )
}

/**
 * Same as calling [insert] with [columnValues] as all columns with '?' values.
 */
/*fun <Table : Any> ModelAdapter<Table>.insertAllAsTemplate(): InsertStart<Table> {
    val (columns, values) = allColumnProperties.map {
        ColumnValue(
            it as Property<Any, Table>,
            Operation.WildCard
        )
    }
        .toColumnValues()
    return InsertImpl(
        columns = columns,
        values = values,
        adapter = this,
    )
}*/


internal data class InsertImpl<Table : Any>(
        override val columns: List<Property<*, Table>>? = null,
        override val values: List<List<Any?>> = listOf(),
        override val conflictAction: ConflictAction = ConflictAction.NONE,
        override val adapter: WritableDBRepresentable<Table>,
        override val subquery: ExecutableQuery<SelectResult<Any>>? = null,
        private val resultFactory: ResultFactory<Long> = InsertResultFactory(adapter),
) : InsertWithValues<Table>, InsertWithConflict<Table>, InsertStart<Table>,
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
                ?.let { propList ->
                    append("(${propList.joinToString { it.query }})")
                }
            if (subquery != null) {
                append(" ${subquery.query}")
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
                        append(list.joinToString { InferredObjectConverter.convert(it) })
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

    override fun <OtherTable : Any> select(subQuery: ExecutableQuery<SelectResult<OtherTable>>): InsertWithSelect<Table, OtherTable> =
        copy(
            subquery = subQuery as ExecutableQuery<SelectResult<Any>>,
        ) as InsertWithSelect<Table, OtherTable>

    override suspend fun execute(db: DatabaseWrapper): Long =
        resultFactory.run { db.createResult(query) }
}
