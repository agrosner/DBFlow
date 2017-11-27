package com.raizlabs.android.dbflow.query

import com.raizlabs.android.dbflow.adapter.InternalAdapter
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.adapter.ModelAdapter
import com.raizlabs.android.dbflow.database.DatabaseStatement

/**
 * Description: Marks a [Model] as subscribing to the [DatabaseStatement]
 * that is used to [Model.insert] a model into the DB.
 */
interface SQLiteStatementListener {

    /**
     * Called at the end of [InternalAdapter.bindToStatement]
     * Perform a custom manipulation of the statement as willed.
     *
     * @param databaseStatement The statement from the [ModelAdapter]
     */
    fun onBindToStatement(databaseStatement: DatabaseStatement)

    /**
     * Called at the end of [InternalAdapter.bindToInsertStatement]
     * Perform a custom manipulation of the statement as willed.
     *
     * @param databaseStatement The insert statement from the [ModelAdapter]
     */
    fun onBindToInsertStatement(databaseStatement: DatabaseStatement)

    /**
     * Called at the end of [InternalAdapter.bindToUpdateStatement]
     * Perform a custom manipulation of the statement as willed.
     *
     * @param databaseStatement The insert statement from the [ModelAdapter]
     */
    fun onBindToUpdateStatement(databaseStatement: DatabaseStatement)

    fun onBindToDeleteStatement(databaseStatement: DatabaseStatement)
}
