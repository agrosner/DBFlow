package com.dbflow5.query

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.longForQuery
import com.dbflow5.runtime.ModelNotification
import com.dbflow5.runtime.NotifyDistributor
import com.dbflow5.stringForQuery
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Determines how results are created from a query.
 */
interface ResultFactory<Result> {

    fun DatabaseWrapper.createResult(query: String): Result
}

/**
 * This runs execute without regard for return type.
 */
object UnitResultFactory : ResultFactory<Unit> {
    override fun DatabaseWrapper.createResult(query: String) =
        compileStatement(query).use { it.execute() }
}

data class UpdateDeleteResultFactory(
    private val table: KClass<*>,
    private val isDelete: Boolean,
) : ResultFactory<Long> {
    override fun DatabaseWrapper.createResult(query: String): Long {
        val affected = compileStatement(query).use { it.executeUpdateDelete() }
        if (affected > 0) {
            NotifyDistributor
                .onChange(
                    this,
                    ModelNotification.TableChange(
                        table,
                        if (isDelete) ChangeAction.DELETE else ChangeAction.UPDATE,
                    )
                )
        }
        return affected
    }
}

data class InsertResultFactory(
    private val table: KClass<*>,
) : ResultFactory<Long> {
    override fun DatabaseWrapper.createResult(query: String): Long {
        val affected = compileStatement(query).use { it.executeInsert() }
        if (affected > 0) {
            NotifyDistributor
                .onChange(
                    this,
                    ModelNotification.TableChange(
                        table,
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

    override fun DatabaseWrapper.createResult(query: String): Count =
        Count(longForQuery(this, query))
}

suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(
    db: DatabaseWrapper
): Boolean = execute(db).value > 0

object StringResultFactory : ResultFactory<StringResultFactory.StringResult> {
    @JvmInline
    value class StringResult(val value: String?)

    override fun DatabaseWrapper.createResult(query: String): StringResult =
        StringResult(stringForQuery(this, query))
}
