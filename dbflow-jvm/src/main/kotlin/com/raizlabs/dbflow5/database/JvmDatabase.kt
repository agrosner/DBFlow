package com.raizlabs.dbflow5.database

import java.sql.Connection

class JvmDatabase(private val connection: Connection) : DatabaseWrapper {

    override val version: Int
        get() = TODO("not implemented")

    override fun execSQL(query: String) {
        connection.prepareStatement(query).execute()
    }

    override fun beginTransaction() {

    }

    override fun setTransactionSuccessful() {
        connection.commit()
    }

    override fun endTransaction() {

    }

    override fun compileStatement(rawQuery: String): DatabaseStatement {
        TODO("not implemented")
    }

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor {
        TODO("not implemented")
    }

    override fun query(tableName: String, columns: Array<String>?, selection: String?, selectionArgs: Array<String>?, groupBy: String?, having: String?, orderBy: String?): FlowCursor {
        TODO("not implemented")
    }

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int {
        TODO("not implemented")
    }
}