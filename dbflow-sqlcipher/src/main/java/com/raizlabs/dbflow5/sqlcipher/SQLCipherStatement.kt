package com.raizlabs.dbflow5.sqlcipher

import com.raizlabs.dbflow5.database.BaseDatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseStatement

import net.sqlcipher.database.SQLiteStatement

/**
 * Description: Implements the methods necessary for [DatabaseStatement]. Delegates calls to
 * the contained [SQLiteStatement].
 */
class SQLCipherStatement
internal constructor(val statement: SQLiteStatement) : BaseDatabaseStatement() {

    override fun executeUpdateDelete(): Long = statement.executeUpdateDelete().toLong()

    override fun execute() {
        statement.execute()
    }

    override fun close() {
        statement.close()
    }

    override fun simpleQueryForLong(): Long {
        return statement.simpleQueryForLong()
    }

    override fun simpleQueryForString(): String? {
        return statement.simpleQueryForString()
    }

    override fun executeInsert(): Long {
        return statement.executeInsert()
    }

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
        fun from(statement: SQLiteStatement): SQLCipherStatement = SQLCipherStatement(statement)
    }
}
