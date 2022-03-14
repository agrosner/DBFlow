package com.dbflow5.query

import com.dbflow5.adapter.WritableDBRepresentable
import com.dbflow5.config.FlowLog
import com.dbflow5.config.Loggable
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.longForQuery
import com.dbflow5.mpp.use
import com.dbflow5.observing.notifications.ModelNotification
import com.dbflow5.stringForQuery
import com.dbflow5.structure.ChangeAction
import kotlin.jvm.JvmInline

internal fun <Result> ResultFactory<Result>.logQuery(query: String) =
    log(FlowLog.Level.D, "Executing query", query)

/**
 * Determines how results are created from a query.
 */
interface ResultFactory<Result> : Loggable {

    fun DatabaseConnection.createResult(query: String): Result
}

/**
 * This runs execute without regard for return type.
 */
object UnitResultFactory : ResultFactory<Unit> {
    override fun DatabaseConnection.createResult(query: String) =
        compileStatement(query).use {
            logQuery(query)
            it.execute()
        }
}

data class UpdateDeleteResultFactory(
    private val dbRepresentable: WritableDBRepresentable<*>,
    private val isDelete: Boolean,
) : ResultFactory<Long> {
    override fun DatabaseConnection.createResult(query: String): Long {
        logQuery(query)
        val affected = compileStatement(query).use { it.executeUpdateDelete() }
        if (affected > 0) {
            generatedDatabase.modelNotifier.enqueueChange(
                ModelNotification.TableChange(
                    dbRepresentable,
                    if (isDelete) ChangeAction.DELETE else ChangeAction.UPDATE,
                )
            )
        }
        return affected
    }
}

data class InsertResultFactory(
    private val dbRepresentable: WritableDBRepresentable<*>,
) : ResultFactory<Long> {
    override fun DatabaseConnection.createResult(query: String): Long {
        logQuery(query)
        val affected = compileStatement(query).use { it.executeInsert() }
        if (affected > 0) {
            generatedDatabase.modelNotifier.enqueueChange(
                ModelNotification.TableChange(
                    dbRepresentable,
                    ChangeAction.INSERT
                )
            )
        }
        return affected
    }
}

object CountResultFactory : ResultFactory<CountResultFactory.Count> {
    @JvmInline
    value class Count(val value: Long)

    override fun DatabaseConnection.createResult(query: String): Count {
        logQuery(query)
        return Count(longForQuery(this, query))
    }
}

suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(
    db: DatabaseConnection
): Boolean = execute(db).value > 0

object StringResultFactory : ResultFactory<StringResultFactory.StringResult> {
    @JvmInline
    value class StringResult(val value: String?)

    override fun DatabaseConnection.createResult(query: String): StringResult {
        logQuery(query)
        return StringResult(stringForQuery(this, query))
    }
}
