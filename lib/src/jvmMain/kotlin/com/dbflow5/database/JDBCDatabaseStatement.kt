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

    override fun executeUpdateDelete(): Long =
        statement.executeUpdate().toLong()

    override fun execute() {
        statement.execute()
    }

    override fun close() {
        statement.close()
    }

    override fun simpleQueryForLong(): Long =
        statement.executeQuery().run {
            if (next()) getLong(0) else 0
        }

    override fun simpleQueryForString(): String? =
        statement.executeQuery().run {
            if (next()) getString(0) else null
        }

    override fun executeInsert(): Long {
        return statement.executeUpdate().toLong()
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
