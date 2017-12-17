package com.raizlabs.dbflow5.query

import android.content.ContentValues
import com.raizlabs.dbflow5.annotation.ConflictAction
import com.raizlabs.dbflow5.appendArray
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.config.modelAdapter
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.property.IProperty
import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Description: The SQLite INSERT command
 */
class Insert<TModel : Any>
/**
 * Constructs a new INSERT command
 *
 * @param table The table to insert into
 */
internal constructor(table: Class<TModel>)
    : BaseQueriable<TModel>(table), Query {

    /**
     * The columns to specify in this query (optional)
     */
    private var columns: List<IProperty<*>>? = null

    /**
     * The values to specify in this query
     */
    private var valuesList: MutableList<MutableList<Any?>> = arrayListOf()

    /**
     * The conflict algorithm to use to resolve inserts.
     */
    private var conflictAction: ConflictAction? = ConflictAction.NONE

    private var selectFrom: From<*>? = null

    override// append FROM, which overrides values
    val query: String
        get() {
            val queryBuilder = StringBuilder("INSERT ")
            if (conflictAction != null && conflictAction != ConflictAction.NONE) {
                queryBuilder.append("OR").append(" $conflictAction ")
            }
            queryBuilder.append("INTO")
                    .append(" ")
                    .append(FlowManager.getTableName(table))

            columns?.let { columns ->
                queryBuilder.append("(")
                        .appendArray(*columns.toTypedArray())
                        .append(")")
            }
            if (selectFrom != null) {
                queryBuilder.append(" ").append(selectFrom!!.query)
            } else {
                if (valuesList.size < 1) {
                    throw IllegalStateException("The insert of " + FlowManager.getTableName(table) + " should have" +
                            "at least one value specified for the insert")
                } else columns?.let { columns ->
                    valuesList.asSequence()
                            .filter { it.size != columns.size }
                            .forEach {
                                throw IllegalStateException(
                                        """The Insert of ${FlowManager.getTableName(table)}
                                            |when specifyingcolumns needs to have the same amount
                                            |of values and columns""".trimMargin())
                            }
                }

                queryBuilder.append(" VALUES(")
                valuesList.forEachIndexed { i, valuesList ->
                    if (i > 0) {
                        queryBuilder.append(",(")
                    }
                    queryBuilder.append(BaseOperator.joinArguments(", ", valuesList)).append(")")
                }
            }

            return queryBuilder.toString()
        }

    override val primaryAction: ChangeAction
        get() = ChangeAction.INSERT

    /**
     * The optional columns to specify. If specified, the values length must correspond to these columns, and
     * each column has a 1-1 relationship to the values.
     *
     * @param columns The columns to use
     */
    fun columns(vararg columns: String) = apply {
        val modelClassModelAdapter = table.modelAdapter
        this.columns = columns.map { modelClassModelAdapter.getProperty(it) }
    }

    fun columns(vararg properties: IProperty<*>) = apply {
        this.columns = properties.toList()
    }

    fun columns(properties: List<IProperty<*>>) = apply {
        this.columns = properties
    }

    /**
     * @return Appends a list of columns to this INSERT statement from the associated [TModel].
     */
    fun asColumns() = apply {
        columns(*table.modelAdapter.allColumnProperties)
    }

    /**
     * @return Appends a list of columns to this INSERT and ? as the values.
     */
    fun asColumnValues() = apply {
        asColumns()
        if (columns != null) {
            val values = arrayListOf<Any?>()
            for (i in columns!!.indices) {
                values.add("?")
            }
            valuesList.add(values)
        }
    }

    /**
     * The required values to specify. It must be non-empty and match the length of the columns when
     * a set of columns are specified.
     *
     * @param values The non type-converted values
     */
    fun values(vararg values: Any) = apply {
        valuesList.add(values.toMutableList())
    }

    /**
     * The required values to specify. It must be non-empty and match the length of the columns when
     * a set of columns are specified.
     *
     * @param values The non type-converted values
     */
    fun values(values: List<Any?>) = apply {
        valuesList.add(values.toMutableList())
    }

    /**
     * Uses the [Operator] pairs to fill this insert query.
     *
     * @param conditions The conditions that we use to fill the columns and values of this INSERT
     */
    fun columnValues(vararg conditions: SQLOperator) = apply {
        val columns = arrayListOf<String>()
        val values = arrayListOf<Any?>()

        for (i in conditions.indices) {
            val condition = conditions[i]
            columns += condition.columnName()
            values += condition.value()
        }

        return columns(*columns.toTypedArray()).values(values)
    }

    /**
     * Uses the [Operator] pairs to fill this insert query.
     *
     * @param operatorGroup The OperatorGroup to use
     */
    fun columnValues(operatorGroup: OperatorGroup) = apply {
        val size = operatorGroup.size
        val columns = arrayListOf<String>()
        val values = arrayListOf<Any?>()

        for (i in 0 until size) {
            val condition = operatorGroup.conditions[i]
            columns += condition.columnName()
            values += condition.value()
        }

        return columns(*columns.toTypedArray()).values(values)
    }

    fun columnValues(contentValues: ContentValues) = apply {
        val entries = contentValues.valueSet()
        val columns = arrayListOf<String>()
        val values = arrayListOf<Any?>()
        for ((key) in entries) {
            columns += key
            values += contentValues.get(key)
        }

        return columns(*columns.toTypedArray()).values(values)
    }

    /**
     * Appends the specified [From], which comes from a [Select] statement.
     *
     * @param selectFrom The from that is continuation of [Select].
     */
    fun select(selectFrom: From<*>) = apply {
        this.selectFrom = selectFrom
    }


    /**
     * Specifies the optional OR method to use for this insert query
     *
     * @param action The conflict action to use
     * @return
     */
    fun or(action: ConflictAction) = apply {
        conflictAction = action
    }

    /**
     * Specifies OR REPLACE, which will either insert if row does not exist, or replace the value if it does.
     *
     * @return
     */
    fun orReplace() = apply {
        return or(ConflictAction.REPLACE)
    }

    /**
     * Specifies OR ROLLBACK, which will cancel the current transaction or ABORT the current statement.
     *
     * @return
     */
    fun orRollback() = apply {
        return or(ConflictAction.ROLLBACK)
    }

    /**
     * Specifies OR ABORT, which will cancel the current INSERT, but all other operations will be preserved in
     * the current transaction.
     *
     * @return
     */
    fun orAbort() = apply {
        return or(ConflictAction.ABORT)
    }

    /**
     * Specifies OR FAIL, which does not back out of the previous statements. Anything else in the current
     * transaction will fail.
     *
     * @return
     */
    fun orFail() = apply {
        return or(ConflictAction.FAIL)
    }

    /**
     * Specifies OR IGNORE, which ignores any kind of error and proceeds as normal.
     *
     * @return
     */
    fun orIgnore() = apply {
        return or(ConflictAction.IGNORE)
    }

    override fun executeUpdateDelete(databaseWrapper: DatabaseWrapper): Long {
        throw IllegalStateException("Cannot call executeUpdateDelete() from an Insert")
    }
}

infix fun <T : Any> Insert<T>.orReplace(into: Array<out Pair<IProperty<*>, *>>) = orReplace().columnValues(*into)

infix fun <T : Any> Insert<T>.orRollback(into: Array<out Pair<IProperty<*>, *>>) = orRollback().columnValues(*into)

infix fun <T : Any> Insert<T>.orAbort(into: Array<out Pair<IProperty<*>, *>>) = orAbort().columnValues(*into)

infix fun <T : Any> Insert<T>.orFail(into: Array<out Pair<IProperty<*>, *>>) = orFail().columnValues(*into)

infix fun <T : Any> Insert<T>.orIgnore(into: Array<out Pair<IProperty<*>, *>>) = orIgnore().columnValues(*into)

infix fun <T : Any> Insert<T>.select(from: From<*>): Insert<T> = select(from)

fun columnValues(vararg pairs: Pair<IProperty<*>, *>): Array<out Pair<IProperty<*>, *>> = pairs

fun <T : Any> Insert<T>.columnValues(vararg pairs: Pair<IProperty<*>, *>): Insert<T> {
    val columns: MutableList<IProperty<*>> = mutableListOf()
    val values = mutableListOf<Any?>()
    pairs.forEach {
        columns.add(it.first)
        values.add(it.second)
    }
    this.columns(columns).values(values)
    return this
}
