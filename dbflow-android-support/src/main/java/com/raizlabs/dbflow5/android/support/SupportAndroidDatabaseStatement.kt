package com.raizlabs.dbflow5.android.support

import android.arch.persistence.db.SupportSQLiteStatement
import com.raizlabs.dbflow5.database.BaseDatabaseStatement
import com.raizlabs.dbflow5.database.rethrowDBFlowException

/**
 * Description: Maps the [SupportSQLiteStatement] to a [SupportAndroidDatabaseStatement]
 */
class SupportAndroidDatabaseStatement(val statement: SupportSQLiteStatement) : BaseDatabaseStatement() {
    override fun executeUpdateDelete(): Long = statement.executeUpdateDelete().toLong()

    override fun execute() {
        statement.execute()
    }

    override fun close() {
        statement.close()
    }

    override fun simpleQueryForLong(): Long = rethrowDBFlowException { statement.simpleQueryForLong() }

    override fun simpleQueryForString(): String? = rethrowDBFlowException { statement.simpleQueryForString() }

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

    companion object {

        @JvmStatic
        fun from(sqLiteStatement: SupportSQLiteStatement): SupportAndroidDatabaseStatement =
            SupportAndroidDatabaseStatement(sqLiteStatement)
    }
}