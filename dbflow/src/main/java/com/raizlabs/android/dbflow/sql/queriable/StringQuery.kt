package com.raizlabs.android.dbflow.sql.queriable

import android.database.sqlite.SQLiteDatabase

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

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
 * @param sql   The sql statement to query the DB with. Does not work with [Delete],
 * this must be done with [SQLiteDatabase.execSQL]
 */
(table: Class<T>,
 /**
  * The full SQLite query to use
  */
 override val query: String) : BaseModelQueriable<T>(table), Query, ModelQueriable<T> {
    private var args: Array<String>? = null

    override// we don't explicitly know the change, but something changed.
    val primaryAction: BaseModel.Action
        get() = BaseModel.Action.CHANGE

    override fun query(): FlowCursor? {
        return query(FlowManager.getDatabaseForTable(table).writableDatabase)
    }

    override fun query(databaseWrapper: DatabaseWrapper): FlowCursor? {
        return databaseWrapper.rawQuery(query, args)
    }

    /**
     * Set selection arguments to execute on this raw query.
     */
    fun setSelectionArgs(args: Array<String>) = apply {
        this.args = args
    }
}
