package com.dbflow5.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement

/**
 * Description:
 */
class AndroidDatabaseStatement
internal constructor(
    val statement: SQLiteStatement,
) : DatabaseStatement {

    override fun executeUpdateDelete(): Long =
        statement.executeUpdateDelete().toLong()

    override fun execute() {
        statement.execute()
    }

    override fun close() {
        statement.close()
    }

    override fun simpleQueryForLong(): Long = rethrowDBFlowException {
        val simpleQueryForLong = statement.simpleQueryForLong()
        simpleQueryForLong
    }

    override fun simpleQueryForString(): String? =
        rethrowDBFlowException { statement.simpleQueryForString() }

    override fun executeInsert(): Long = rethrowDBFlowException { statement.executeInsert() }

    override fun bindString(index: Int, s: String) {
        statement.bindString(index, s)
    }

    override fun bindNull(index: Int) {
        statement.bindNull(index)
    }

    override fun bindLong(index: Int, aLong: Long) {
        statement.bindLong(index, aLong)
    }

    override fun bindDouble(index: Int, aDouble: Double) {
        statement.bindDouble(index, aDouble)
    }

    override fun bindBlob(index: Int, bytes: ByteArray) {
        statement.bindBlob(index, bytes)
    }

    override fun bindAllArgsAsStrings(selectionArgs: Array<String>?) {
        statement.bindAllArgsAsStrings(selectionArgs)
    }

    companion object {

        @Deprecated(
            replaceWith = ReplaceWith("from(sqLiteStatement)"),
            message = "Database no longer needed as parameter. You can remove."
        )
        @JvmStatic
        fun from(
            sqLiteStatement: SQLiteStatement,
            database: SQLiteDatabase
        ): AndroidDatabaseStatement =
            AndroidDatabaseStatement(sqLiteStatement)

        @JvmStatic
        fun from(sqLiteStatement: SQLiteStatement) = AndroidDatabaseStatement(sqLiteStatement)
    }
}
