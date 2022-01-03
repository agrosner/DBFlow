package com.dbflow5.query

import com.dbflow5.adapter.InternalAdapter
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.structure.Model

/**
 * Description: Marks a [Model] as subscribing to the [DatabaseStatement]
 * that is used to [Model.insert] a model into the DB.
 */
interface SQLiteStatementListener {

    /**
     * Called at the end of [InternalAdapter.bindToInsertStatement]
     * Perform a custom manipulation of the statement as willed.
     *
     * @param databaseStatement The insert statement from the [ModelAdapter]
     */
    fun onBindToInsertStatement(databaseStatement: DatabaseStatement) = Unit

    /**
     * Called at the end of [InternalAdapter.bindToUpdateStatement]
     * Perform a custom manipulation of the statement as willed.
     *
     * @param databaseStatement The insert statement from the [ModelAdapter]
     */
    fun onBindToUpdateStatement(databaseStatement: DatabaseStatement) = Unit

    fun onBindToDeleteStatement(databaseStatement: DatabaseStatement) = Unit
}
