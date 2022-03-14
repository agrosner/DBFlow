package com.dbflow5.database

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.writableTransaction
import com.dbflow5.delegates.CheckOpen

/**
 * An abstraction layer on top of a specific database connection.
 */
interface DatabaseConnection : CheckOpen {

    val generatedDatabase: GeneratedDatabase

    val isInTransaction: Boolean

    /**
     * The current version of the database.
     */
    val version: Int

    /**
     * Execute an arbitrary SQL query.
     */
    fun execute(query: String)

    /**
     * Executes a transaction. Its discouraged to call this method directly.
     * use the [writableTransaction] method directly.
     *
     * This may run from:
     * 1. on the [generatedDatabase.transactionDispatcher] for main DB operations
     * 2. blocking thread during db creation + construction.
     */
    @InternalDBFlowApi
    suspend fun <R> executeTransaction(dbFn: suspend DatabaseConnection.() -> R): R

    /**
     * For a given query, return a [DatabaseStatement].
     */
    fun compileStatement(rawQuery: String): DatabaseStatement

    /**
     * For given query and selection args, return a [FlowCursor] to retrieve data.
     */
    fun rawQuery(query: String): FlowCursor
}
