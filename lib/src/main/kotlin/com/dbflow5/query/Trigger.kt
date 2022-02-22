package com.dbflow5.query

import com.dbflow5.adapter2.DBRepresentable
import com.dbflow5.appendQuotedIfNeeded
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.operations.AnyOperator
import com.dbflow5.query.operations.Property
import com.dbflow5.sql.Query

internal sealed class TriggerQualifier(val value: String) {
    object Before : TriggerQualifier("BEFORE")
    object After : TriggerQualifier("AFTER")
    object InsteadOf : TriggerQualifier("INSTEAD OF")

    companion object {
        val All = listOf(Before, After, InsteadOf)
    }
}

internal sealed class TriggerMethod(val value: String) {
    object Insert : TriggerMethod("INSERT")
    data class Update<Table : Any>(val properties: List<Property<*, Table>>) :
        TriggerMethod("UPDATE")

    object Delete : TriggerMethod("DELETE")
    companion object {
        val All = listOf(Insert, Update<Any>(listOf()), Delete)
    }
}

interface IsTriggerQualified<Table : Any> {
    /**
     * Specifies that we should do this TRIGGER before some event
     */
    fun before(): TriggerQualified<Table>

    /**
     * Specifies that we should do this TRIGGER after some event
     */
    fun after(): TriggerQualified<Table>

    /**
     * Specifies that we should do this TRIGGER instead of the specified events
     */
    fun insteadOf(): TriggerQualified<Table>
}

interface TriggerMethodEnabled<Table : Any> {
    fun insertOn(): TriggerOn<Table>

    /**
     * If [ofProperties] specified, this UPDATE becomes UPDATE OF p0,p1,...
     */
    fun updateOn(vararg ofProperties: Property<*, Table>): TriggerOn<Table>
    fun deleteOn(): TriggerOn<Table>
}

interface TriggerForEachRowEnabled<Table : Any> {
    fun forEachRow(): TriggerForEachRow<Table>
}

interface TriggerWhenEnabled<Table : Any> {
    infix fun whenever(operator: AnyOperator): TriggerWhen<Table>
}

interface TriggerLogicEnabled<Table : Any> {
    infix fun begin(query: Query): TriggerLogic<Table>
}

interface Trigger<Table : Any> : Query,
    HasAdapter<Table, DBRepresentable<Table>>

interface TriggerStart<Table : Any> : Trigger<Table>,
    IsTriggerQualified<Table>,
    TriggerMethodEnabled<Table>

interface TriggerQualified<Table : Any> : Trigger<Table>,
    TriggerMethodEnabled<Table>

interface TriggerOn<Table : Any> : Trigger<Table>,
    TriggerForEachRowEnabled<Table>,
    TriggerWhenEnabled<Table>,
    TriggerLogicEnabled<Table>

interface TriggerForEachRow<Table : Any> : Trigger<Table>,
    TriggerWhenEnabled<Table>,
    TriggerLogicEnabled<Table>

interface TriggerWhen<Table : Any> : Trigger<Table>,
    TriggerLogicEnabled<Table>

interface TriggerLogic<Table : Any> : Trigger<Table>,
    ExecutableQuery<Unit> {
    infix fun and(query: Query): TriggerLogic<Table>
}

/**
 * Starts a TRIGGER statement.
 */
fun <Table : Any> DBRepresentable<Table>.createTrigger(
    name: String,
    temporary: Boolean = false,
    ifNotExists: Boolean = true,
): TriggerStart<Table> {
    return TriggerImpl(
        name = name,
        temporary = temporary,
        ifNotExists = ifNotExists,
        adapter = this,
    )
}

internal data class TriggerImpl<Table : Any>(
    override val adapter: DBRepresentable<Table>,
    private val name: String,
    private val temporary: Boolean,
    private val ifNotExists: Boolean,
    private val qualifier: TriggerQualifier? = null,
    private val method: TriggerMethod? = null,
    private val forEachRow: Boolean = false,
    private val whenOperator: AnyOperator? = null,
    private val triggerLogicQuery: List<Query> = emptyList(),
) : TriggerStart<Table>,
    TriggerQualified<Table>,
    TriggerOn<Table>,
    TriggerForEachRow<Table>,
    TriggerWhen<Table>,
    TriggerLogic<Table> {
    override val query: String by lazy {
        buildString {
            append("CREATE ")
            if (temporary) append("TEMP ")
            append("TRIGGER ")
            if (ifNotExists) append("IF NOT EXISTS ")
            appendQuotedIfNeeded(name).append(" ")
            if (qualifier != null) append("${qualifier.value} ")
            if (method != null) append("${method.value} ")
            if (method is TriggerMethod.Update<*> &&
                method.properties.isNotEmpty()
            ) {
                append("OF ${method.properties.joinToString { it.query }} ")
            }
            append("ON ${adapter.name} ")
            if (forEachRow) append("FOR EACH ROW ")
            whenOperator?.let { append("WHEN ${it.query} ") }
            if (triggerLogicQuery.isNotEmpty()) {
                append(
                    "\nBEGIN\n${
                        triggerLogicQuery.joinToString(separator = ";\n") { it.query }
                    };\nEND"
                )
            }
        }
    }

    override fun insertOn(): TriggerOn<Table> =
        copy(
            method = TriggerMethod.Insert,
        )

    override fun updateOn(vararg ofProperties: Property<*, Table>): TriggerOn<Table> =
        copy(
            method = TriggerMethod.Update(ofProperties.toList())
        )

    override fun deleteOn(): TriggerOn<Table> =
        copy(
            method = TriggerMethod.Delete,
        )

    override fun before(): TriggerQualified<Table> =
        copy(
            qualifier = TriggerQualifier.Before,
        )

    override fun after(): TriggerQualified<Table> =
        copy(
            qualifier = TriggerQualifier.After,
        )

    override fun insteadOf(): TriggerQualified<Table> =
        copy(
            qualifier = TriggerQualifier.InsteadOf,
        )

    override fun forEachRow(): TriggerForEachRow<Table> =
        copy(
            forEachRow = true,
        )

    override fun whenever(operator: AnyOperator): TriggerWhen<Table> =
        copy(
            whenOperator = operator,
        )

    override fun begin(query: Query): TriggerLogic<Table> =
        copy(
            triggerLogicQuery = listOf(query),
        )

    override fun and(query: Query): TriggerLogic<Table> =
        copy(
            triggerLogicQuery = triggerLogicQuery.toMutableList().apply { add(query) }
        )

    override suspend fun execute(db: DatabaseWrapper) {
        db.execSQL(query)
    }
}