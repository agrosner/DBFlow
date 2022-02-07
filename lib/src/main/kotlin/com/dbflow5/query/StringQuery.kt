package com.dbflow5.query

import android.database.sqlite.SQLiteDatabase
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseStatementWrapper
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.sql.Query
import com.dbflow5.structure.ChangeAction
import kotlinx.coroutines.runBlocking

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL cursor string
 * code where this library does not provide. It only runs a
 * [SQLiteDatabase.rawQuery].
 */
class StringQuery<T : Any>
/**
 * Creates an instance of this class
 *
 * @param table The table to use
 * @param query   The sql statement to query the DB with. Does not work with [Delete],
 * this must be done with [DatabaseWrapper.execSQL]
 */
    (
    adapter: RetrievalAdapter<T>,
    override val query: String
) : BaseModelQueriable<T>(adapter), Query, ModelQueriable<T> {
    private var args: Array<String>? = null

    override// we don't explicitly know the change, but something changed.
    val primaryAction: ChangeAction
        get() = ChangeAction.CHANGE

    override fun cursor(databaseWrapper: DatabaseWrapper): FlowCursor =
        databaseWrapper.rawQuery(query, args)

    override fun queryList(databaseWrapper: DatabaseWrapper): List<T> {
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return runBlocking { adapter.loadList(cursor(databaseWrapper), databaseWrapper) }
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): T? {
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return runBlocking { adapter.loadSingle(cursor(databaseWrapper), databaseWrapper) }
    }

    override fun <QueryClass : Any> queryCustomList(
        retrievalAdapter: RetrievalAdapter<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : List<QueryClass> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return runBlocking { retrievalAdapter.loadList(cursor(databaseWrapper), databaseWrapper) }
    }

    override fun <QueryClass : Any> queryCustomSingle(
        retrievalAdapter: RetrievalAdapter<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : QueryClass? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return runBlocking { retrievalAdapter.loadSingle(cursor(databaseWrapper), databaseWrapper) }
    }

    override fun compileStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement {
        val query = query
        FlowLog.log(
            FlowLog.Level.V,
            "Compiling Query Into Statement: query = $query , args = $args"
        )
        return DatabaseStatementWrapper(
            databaseWrapper.compileStatement(query, args), this,
            databaseWrapper
        )
    }

    /**
     * Set selection arguments to execute on this raw query.
     */
    fun setSelectionArgs(args: Array<String>) = apply {
        this.args = args
    }
}
