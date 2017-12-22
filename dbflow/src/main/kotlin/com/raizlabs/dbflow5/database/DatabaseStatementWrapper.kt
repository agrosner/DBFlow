package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.query.BaseQueriable
import com.raizlabs.dbflow5.runtime.NotifyDistributor

/**
 * Description: Delegates all of its calls to the contained [DatabaseStatement], while
 * providing notification methods for when operations occur.
 */
class DatabaseStatementWrapper<T : Any>(
    private val databaseStatement: DatabaseStatement,
    private val modelQueriable: BaseQueriable<T>) : BaseDatabaseStatement() {

    override fun executeUpdateDelete(): Long {
        val affected = databaseStatement.executeUpdateDelete()
        if (affected > 0) {
            NotifyDistributor.get().notifyTableChanged(modelQueriable.table,
                modelQueriable.primaryAction)
        }
        return affected
    }

    override fun execute() {
        databaseStatement.execute()
    }

    override fun close() {
        databaseStatement.close()
    }

    override fun simpleQueryForLong(): Long {
        return databaseStatement.simpleQueryForLong()
    }

    override fun simpleQueryForString(): String? {
        return databaseStatement.simpleQueryForString()
    }

    override fun executeInsert(): Long {
        val affected = databaseStatement.executeInsert()
        if (affected > 0) {
            NotifyDistributor.get().notifyTableChanged(modelQueriable.table,
                modelQueriable.primaryAction)
        }
        return affected
    }

    override fun bindString(index: Int, s: String) {
        databaseStatement.bindString(index, s)
    }

    override fun bindNull(index: Int) {
        databaseStatement.bindNull(index)
    }

    override fun bindLong(index: Int, aLong: Long) {
        databaseStatement.bindLong(index, aLong)
    }

    override fun bindDouble(index: Int, aDouble: Double) {
        databaseStatement.bindDouble(index, aDouble)
    }

    override fun bindBlob(index: Int, bytes: ByteArray) {
        databaseStatement.bindBlob(index, bytes)
    }

    override fun bindAllArgsAsStrings(selectionArgs: Array<String>?) {
        databaseStatement.bindAllArgsAsStrings(selectionArgs)
    }
}
