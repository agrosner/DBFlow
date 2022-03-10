package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.delegates.CheckOpen

/**
 * Description: Provides a base implementation that wraps a database, so other databaseForTable engines potentially can
 * be used.
 */
interface DatabaseWrapper : CheckOpen {

    val generatedDatabase: GeneratedDatabase

    val isInTransaction: Boolean

    /**
     * The current version of the database.
     */
    val version: Int

    /**
     * Execute an arbitrary SQL query.
     */
    fun execSQL(query: String)

    /**
     * Executes a transaction.
     */
    suspend fun <R> executeTransaction(dbFn: suspend DatabaseWrapper.() -> R): R

    /**
     * For a given query, return a [DatabaseStatement].
     */
    fun compileStatement(rawQuery: String): DatabaseStatement

    /**
     * For given query and selection args, return a [FlowCursor] to retrieve data.
     */
    fun rawQuery(query: String): FlowCursor
}
