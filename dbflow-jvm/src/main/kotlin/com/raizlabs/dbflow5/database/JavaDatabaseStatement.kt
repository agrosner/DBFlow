package com.raizlabs.dbflow5.database

import com.almworks.sqlite4java.SQLiteStatement

class JavaDatabaseStatement internal constructor(val statement: SQLiteStatement) : BaseDatabaseStatement(), DatabaseStatement {

    override fun executeUpdateDelete(): Long {
        rethrowDBFlowException {
            if (!statement.step()) {
                return 0
            }
        }
    }

    override fun execute() {
        if (statement.hasStepped()) {
            statement.reset(false)
        }
        statement.step()
    }

    override fun close() {
        statement.dispose()
    }

    override fun simpleQueryForLong(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun simpleQueryForString(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun executeInsert(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bindString(index: Int, s: String) {
        statement.bind(index, s)
    }

    override fun bindNull(index: Int) {
        statement.bindNull(index)
    }

    override fun bindLong(index: Int, aLong: Long) {
        statement.bind(index, aLong)
    }

    override fun bindDouble(index: Int, aDouble: Double) {
        statement.bind(index, aDouble)
    }

    override fun bindBlob(index: Int, bytes: ByteArray) {
        statement.bind(index, bytes)
    }

    override fun bindAllArgsAsStrings(selectionArgs: Array<String>?) {
        selectionArgs?.let { selectionArgs.forEachIndexed { index, value -> bindString(index, value) } }
    }

    companion object {

        @JvmStatic
        fun from(sqLiteStatement: SQLiteStatement): DatabaseStatement = JavaDatabaseStatement(sqLiteStatement)
    }
}
