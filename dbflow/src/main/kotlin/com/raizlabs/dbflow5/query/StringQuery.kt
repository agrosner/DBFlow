package com.raizlabs.dbflow5.query

import android.database.sqlite.SQLiteDatabase
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL query string
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
(databaseWrapper: DatabaseWrapper,
 table: Class<T>,
 override val query: String)
    : BaseModelQueriable<T>(databaseWrapper, table), Query, ModelQueriable<T> {
    private var args: Array<String>? = null

    override// we don't explicitly know the change, but something changed.
    val primaryAction: ChangeAction
        get() = ChangeAction.CHANGE

    override fun query(): FlowCursor? = databaseWrapper.rawQuery(query, args)

    /**
     * Set selection arguments to execute on this raw query.
     */
    fun setSelectionArgs(args: Array<String>) = apply {
        this.args = args
    }
}
