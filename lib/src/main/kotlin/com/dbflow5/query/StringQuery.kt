package com.dbflow5.query

import android.database.sqlite.SQLiteDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseStatementWrapper
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.sql.Query
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

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
    table: KClass<T>,
    override val query: String
) : BaseModelQueriable<T>(table), Query, ModelQueriable<T> {
    private var args: Array<String>? = null

    override// we don't explicitly know the change, but something changed.
    val primaryAction: ChangeAction
        get() = ChangeAction.CHANGE

    override fun cursor(databaseWrapper: DatabaseWrapper): FlowCursor? =
        databaseWrapper.rawQuery(query, args)

    override fun queryList(databaseWrapper: DatabaseWrapper): MutableList<T> {
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return listModelLoader.load(cursor(databaseWrapper), databaseWrapper)!!
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): T? {
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return singleModelLoader.load(cursor(databaseWrapper), databaseWrapper)!!
    }

    override fun <QueryClass : Any> queryCustomList(
        queryModelClass: KClass<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : MutableList<QueryClass> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return getListQueryModelLoader(queryModelClass)
            .load(cursor(databaseWrapper), databaseWrapper)!!
    }

    override fun <QueryClass : Any> queryCustomSingle(
        queryModelClass: KClass<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : QueryClass? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return getSingleQueryModelLoader(queryModelClass)
            .load(cursor(databaseWrapper), databaseWrapper)
    }

    override fun compileStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement {
        val query = query
        FlowLog.log(
            FlowLog.Level.V,
            "Compiling Query Into Statement: query = $query , args = $args"
        )
        return DatabaseStatementWrapper(databaseWrapper.compileStatement(query, args), this)
    }

    /**
     * Set selection arguments to execute on this raw query.
     */
    fun setSelectionArgs(args: Array<String>) = apply {
        this.args = args
    }
}
