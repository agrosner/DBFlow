package com.dbflow5.database

import co.touchlab.sqliter.Statement
import co.touchlab.sqliter.longForQuery
import co.touchlab.sqliter.stringForQuery

/**
 * Description:
 */
@Suppress("DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES")
class NativeDatabaseStatement internal constructor(
    private val statement: Statement,
) : DatabaseStatement {
    override fun executeUpdateDelete(): Long = rethrowDBFlowException {
        statement.executeUpdateDelete().toLong()
    }

    override fun execute() = rethrowDBFlowException { statement.execute() }

    override fun close() {
        statement.clearBindings()
    }

    override fun simpleQueryForLong(): Long = rethrowDBFlowException {
        statement.longForQuery()
    }

    override fun simpleQueryForString(): String = rethrowDBFlowException {
        statement.stringForQuery()
    }

    override fun executeInsert(): Long = rethrowDBFlowException { statement.executeInsert() }

    override fun bindString(index: Int, s: String) = statement.bindString(index, s)

    override fun bindNull(index: Int) = statement.bindNull(index)

    override fun bindLong(index: Int, aLong: Long) = statement.bindLong(index, aLong)

    override fun bindDouble(index: Int, aDouble: Double) = statement.bindDouble(index, aDouble)

    override fun bindBlob(index: Int, bytes: ByteArray) = statement.bindBlob(index, bytes)
}
