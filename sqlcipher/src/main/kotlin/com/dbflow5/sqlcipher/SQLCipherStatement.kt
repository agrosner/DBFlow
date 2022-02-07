package com.dbflow5.sqlcipher

import com.dbflow5.database.DatabaseStatement
import net.sqlcipher.database.SQLiteStatement

/**
 * Description: Implements the methods necessary for [DatabaseStatement]. Delegates calls to
 * the contained [SQLiteStatement].
 */
class SQLCipherStatement
internal constructor(val statement: SQLiteStatement) : DatabaseStatement {

    override fun executeUpdateDelete(): Long =
        rethrowDBFlowException { statement.executeUpdateDelete().toLong() }

    override fun execute() {
        statement.execute()
    }

    override fun close() {
        statement.close()
    }

    override fun simpleQueryForLong(): Long =
        rethrowDBFlowException { statement.simpleQueryForLong() }

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
        if (selectionArgs != null) {
            for (i in selectionArgs.size downTo 1) {
                bindString(i, selectionArgs[i - 1])
            }
        }
    }

    companion object {

        @JvmStatic
        fun from(statement: SQLiteStatement): SQLCipherStatement = SQLCipherStatement(statement)
    }
}
