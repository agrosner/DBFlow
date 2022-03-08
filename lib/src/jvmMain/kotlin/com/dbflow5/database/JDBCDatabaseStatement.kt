package com.dbflow5.database

import java.sql.PreparedStatement
import java.sql.Types
import javax.sql.rowset.serial.SerialBlob

/**
 * Reimplements Android's version.
 */
class JDBCDatabaseStatement
internal constructor(
    internal val statement: PreparedStatement,
) : DatabaseStatement {

    override fun executeUpdateDelete(): Long = rethrowDBFlowException {
        statement.executeUpdate().toLong()
    }

    override fun execute() = rethrowDBFlowException {
        statement.execute()
        Unit
    }

    override fun close() {
        statement.close()
    }

    override fun simpleQueryForLong(): Long = rethrowDBFlowException {
        statement.executeQuery().run {
            if (next()) getLong(1) else 0
        }
    }

    override fun simpleQueryForString(): String? = rethrowDBFlowException {
        statement.executeQuery().run {
            if (next()) getString(1) else null
        }
    }

    override fun executeInsert(): Long = rethrowDBFlowException {
        // retrieve the first generated key as return type.
        statement.executeUpdate()
        return JDBCFlowCursor(statement.generatedKeys).use {
            it.getLong(0)
        }
    }

    override fun bindString(index: Int, s: String) {
        statement.setString(index, s)
    }

    override fun bindNull(index: Int) {
        statement.setNull(index, Types.VARCHAR)
    }

    override fun bindLong(index: Int, aLong: Long) {
        statement.setLong(index, aLong)
    }

    override fun bindDouble(index: Int, aDouble: Double) {
        statement.setDouble(index, aDouble)
    }

    override fun bindBlob(index: Int, bytes: ByteArray) {
        statement.setBlob(index, SerialBlob(bytes))
    }

    override fun bindAllArgsAsStrings(selectionArgs: Array<String>?) {
        selectionArgs?.forEachIndexed { index, value -> bindString(index, value) }
    }

    companion object {

        @JvmStatic
        fun from(
            statement: PreparedStatement,
        ): JDBCDatabaseStatement = JDBCDatabaseStatement(statement)
    }
}
